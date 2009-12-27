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

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

public class NettyRpcController implements RpcController {

	private String reason;
	private boolean failed;
	private boolean canceled;
	@SuppressWarnings("unused")
	private RpcCallback<Object> callback;
	
	public String errorText() {
		return reason;
	}

	public boolean failed() {
		return failed;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void notifyOnCancel(RpcCallback<Object> callback) {
		this.callback = callback;
	}

	public void reset() {
		reason = null;
		failed = false;
		canceled = false;
		callback = null;
	}

	public void setFailed(String reason) {
		this.reason = reason;
		this.failed = true;
	}

	public void startCancel() {
		canceled = true;
	}

}
