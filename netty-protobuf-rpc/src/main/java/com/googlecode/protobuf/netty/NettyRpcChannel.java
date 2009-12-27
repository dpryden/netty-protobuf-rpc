/*
 * Copyright (c) 2009 Stephen Tu <stephen_tu@berkeley.edu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.googlecode.protobuf.netty;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.googlecode.protobuf.netty.NettyRpcProto.RpcRequest;
import com.googlecode.protobuf.netty.NettyRpcProto.RpcResponse;

public class NettyRpcChannel implements RpcChannel, BlockingRpcChannel {

	private static final Logger logger = Logger.getLogger(NettyRpcChannel.class);
	
	private final Channel channel;
	private final NettyRpcClientChannelUpstreamHandler handler;
	
	public NettyRpcChannel(Channel channel) {
		this.channel = channel;
		this.handler = channel.getPipeline().get(NettyRpcClientChannelUpstreamHandler.class);
		if (handler == null) {
			throw new IllegalArgumentException("Channel does not have proper handler");
		}
	}
	
	public RpcController newRpcController() {
		return new NettyRpcController();
	}
	
	public void callMethod(MethodDescriptor method, RpcController controller,
			Message request, Message responsePrototype, RpcCallback<Message> done) {
		int nextSeqId = (done == null) ? -1 : handler.getNextSeqId();
		Message rpcRequest = buildRequest(done != null, nextSeqId, false, method, request);
		if (done != null) {
			handler.registerCallback(nextSeqId, new ResponsePrototypeRpcCallback(controller, responsePrototype, done));
		}
		channel.write(rpcRequest);
	}

	public Message callBlockingMethod(MethodDescriptor method,
			RpcController controller, Message request, Message responsePrototype)
			throws ServiceException {
		logger.debug("calling blocking method: " + method.getFullName());
		BlockingRpcCallback callback = new BlockingRpcCallback();
		ResponsePrototypeRpcCallback rpcCallback = new ResponsePrototypeRpcCallback(controller, responsePrototype, callback);
		int nextSeqId = handler.getNextSeqId();
		Message rpcRequest = buildRequest(true, nextSeqId, true, method, request);
		handler.registerCallback(nextSeqId, rpcCallback);
		channel.write(rpcRequest);
		synchronized(callback) {
			while(!callback.isDone()) {
				try {
					callback.wait();
				} catch (InterruptedException e) {
					logger.warn("Interrupted while blocking", e);
					/* Ignore */
				}
			}
		}
		if (rpcCallback.getRpcResponse() != null && rpcCallback.getRpcResponse().hasErrorCode()) {
			// TODO: should we only throw this if the error code matches the 
			// case where the server call threw a ServiceException?
			throw new ServiceException(rpcCallback.getRpcResponse().getErrorMessage());
		}
		return callback.getMessage();
	}
	
	public void close() {
		channel.close().awaitUninterruptibly();
	}

	private Message buildRequest(boolean hasSequence, int seqId, boolean isBlocking, MethodDescriptor method, Message request) {
		RpcRequest.Builder requestBuilder = RpcRequest.newBuilder();
		if (hasSequence) {
			requestBuilder.setId(seqId);
		}
		return requestBuilder
			.setIsBlockingService(isBlocking)
			.setServiceName(method.getService().getFullName())
			.setMethodName(method.getName())
			.setRequestMessage(request.toByteString())
			.build();
	}
	
	static class ResponsePrototypeRpcCallback implements RpcCallback<RpcResponse> {
		
		private final RpcController controller;
		private final Message responsePrototype;
		private final RpcCallback<Message> callback; 
		
		private RpcResponse rpcResponse;
		
		public ResponsePrototypeRpcCallback(RpcController controller, Message responsePrototype, RpcCallback<Message> callback) {
			if (responsePrototype == null) {
				throw new IllegalArgumentException("Must provide response prototype");
			} else if (callback == null) {
				throw new IllegalArgumentException("Must provide callback");
			}
			this.controller = controller;
			this.responsePrototype = responsePrototype;
			this.callback = callback;
		}
		
		public void run(RpcResponse message) {
			rpcResponse = message;
			try {
				Message response = (message == null || !message.hasResponseMessage()) ? 
						null : 
						responsePrototype.newBuilderForType().mergeFrom(message.getResponseMessage()).build();
				callback.run(response);
			} catch (InvalidProtocolBufferException e) {
				logger.warn("Could not marshall into response", e);
				if (controller != null) {
					controller.setFailed("Received invalid response type from server");
				}
				callback.run(null);
			}
		}
		
		public RpcController getRpcController() {
			return controller;
		}
		
		public RpcResponse getRpcResponse() {
			return rpcResponse;
		}
		
	}
	
	private static class BlockingRpcCallback implements RpcCallback<Message> {

		private boolean done = false;
		private Message message;
		
		public void run(Message message) {
			this.message = message;
			synchronized(this) {
				done = true;
				notify();
			}
		}
		
		public Message getMessage() {
			return message;
		}
		
		public boolean isDone() {
			return done;
		}
		
	}
	
}
