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
package com.googlecode.protobuf.netty.example;

import org.apache.log4j.Logger;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.netty.example.Calculator.CalcRequest;
import com.googlecode.protobuf.netty.example.Calculator.CalcResponse;

public class CalculatorServiceImpl implements Calculator.CalcService.Interface, Calculator.CalcService.BlockingInterface {

	private static final Logger logger = Logger.getLogger(CalculatorServiceImpl.class);
	
	public void add(RpcController controller, CalcRequest request,
			RpcCallback<CalcResponse> done) {
		CalcResponse response = null;
		try {
			response = add(controller, request);
		} catch (ServiceException e) { }
		if (done != null) {
			done.run(response);
		} else {
			// Since no callback registered, might as well spit out the
			// answer here otherwise we'll never know what happened
			if (response == null)
				logger.info("Error occured");
			else
				logger.info("Answer is: " + response.getResult());
		}
	}

	public void divide(RpcController controller, CalcRequest request,
			RpcCallback<CalcResponse> done) {
		CalcResponse response = null;
		try {
			response = divide(controller, request);
		} catch (ServiceException e) { }
		if (done != null) {
			done.run(response);
		} else {
			// Since no callback registered, might as well spit out the
			// answer here otherwise we'll never know what happened
			if (response == null)
				logger.info("Error occured");
			else
				logger.info("Answer is: " + response.getResult());
		}
	}

	public void multiply(RpcController controller, CalcRequest request,
			RpcCallback<CalcResponse> done) {
		CalcResponse response = null;
		try {
			response = multiply(controller, request);
		} catch (ServiceException e) { }
		if (done != null) {
			done.run(response);
		} else {
			// Since no callback registered, might as well spit out the
			// answer here otherwise we'll never know what happened
			if (response == null)
				logger.info("Error occured");
			else
				logger.info("Answer is: " + response.getResult());
		}
	}

	public void subtract(RpcController controller, CalcRequest request,
			RpcCallback<CalcResponse> done) {
		CalcResponse response = null;
		try {
			response = subtract(controller, request);
		} catch (ServiceException e) { }
		if (done != null) {
			done.run(response);
		} else {
			// Since no callback registered, might as well spit out the
			// answer here otherwise we'll never know what happened
			if (response == null)
				logger.info("Error occured");
			else
				logger.info("Answer is: " + response.getResult());
		}
	}

	public CalcResponse add(RpcController controller, CalcRequest request)
			throws ServiceException {
		int answer = request.getOp1() + request.getOp2();
		return CalcResponse.newBuilder().setResult(answer).build();
	}

	public CalcResponse divide(RpcController controller, CalcRequest request)
			throws ServiceException {
		if (request.getOp2() == 0) {
			controller.setFailed("Cannot divide by zero");
			throw new ServiceException("Cannot divide by zero");
		}
		int answer = request.getOp1() / request.getOp2();
		return CalcResponse.newBuilder().setResult(answer).build();
	}

	public CalcResponse multiply(RpcController controller, CalcRequest request)
			throws ServiceException {
		int answer = request.getOp1() * request.getOp2();
		return CalcResponse.newBuilder().setResult(answer).build();
	}

	public CalcResponse subtract(RpcController controller, CalcRequest request)
			throws ServiceException {
		int answer = request.getOp1() - request.getOp2();
		return CalcResponse.newBuilder().setResult(answer).build();
	}

}
