/*
 * Copyright (c) 2009 David Grant
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

package com.google.code.jscep.transport;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Logger;

import com.google.code.jscep.request.Operation;
import com.google.code.jscep.request.Request;
import com.google.code.jscep.util.LoggingUtil;

/**
 * Transport representing the <tt>HTTP GET</tt> method
 */
public class HttpGetTransport extends Transport {
	private static Logger LOGGER = LoggingUtil.getLogger("com.google.code.jscep.transport");

	HttpGetTransport(URL url, Proxy proxy) {
		super(url, proxy);
	}

	@Override
	public <T> T sendMessage(Request<T> msg) throws IOException {
		LOGGER.entering(getClass().getName(), "sendMessage", msg);
		
		final URL url = getUrl(msg.getOperation(), msg.getMessage());
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);

		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			IOException ioe = new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
			
			LOGGER.throwing(getClass().getName(), "sendMessage", ioe);
			throw ioe;
		}

		final T response = msg.getContentHandler().getContent(conn.getInputStream(), conn.getContentType());
		
		LOGGER.exiting(getClass().getName(), "sendMessage", response);
		return response;
	}

	private URL getUrl(Operation op, Object message)
			throws MalformedURLException {
		if (message == null) {
			return new URL(getUrl(op).toExternalForm() + "&message=");
		} else {
			// TODO: Does this encode bytes?
			return new URL(getUrl(op).toExternalForm() + "&message=" + message);
		}
	}
	
	@Override
	public String toString() {
		if (proxy == Proxy.NO_PROXY) {
			return "HTTP GET Transport for " + url;
		} else {
			return "HTTP GET Transport for " + url + " (using " + proxy + ")";
		}
	}
}