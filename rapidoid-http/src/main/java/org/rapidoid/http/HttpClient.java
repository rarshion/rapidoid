package org.rapidoid.http;

/*
 * #%L
 * rapidoid-http
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.rapidoid.concurrent.Callback;
import org.rapidoid.concurrent.Callbacks;
import org.rapidoid.concurrent.Future;
import org.rapidoid.concurrent.Promise;
import org.rapidoid.concurrent.Promises;
import org.rapidoid.io.IO;
import org.rapidoid.log.Log;
import org.rapidoid.util.U;

public class HttpClient {

	private final CloseableHttpAsyncClient client;

	public HttpClient() {
		this(HttpAsyncClients.createDefault());
	}

	public HttpClient(CloseableHttpAsyncClient client) {
		this.client = client;
		client.start();
	}

	public Future<byte[]> post(String uri, Map<String, String> headers, Map<String, String> data,
			Map<String, String> files, Callback<byte[]> callback) {

		headers = U.safe(headers);
		data = U.safe(data);
		files = U.safe(files);

		HttpPost httppost = new HttpPost(uri);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		for (Entry<String, String> entry : files.entrySet()) {
			ContentType contentType = ContentType.create("application/octet-stream");
			String filename = entry.getValue();
			File file = IO.file(filename);
			builder = builder.addBinaryBody(entry.getKey(), file, contentType, filename);
		}

		for (Entry<String, String> entry : data.entrySet()) {
			ContentType contentType = ContentType.create("text/plain", "UTF-8");
			builder = builder.addTextBody(entry.getKey(), entry.getValue(), contentType);
		}

		httppost.setEntity(builder.build());

		for (Entry<String, String> e : headers.entrySet()) {
			httppost.addHeader(e.getKey(), e.getValue());
		}

		Log.debug("Starting HTTP POST request", "request", httppost.getRequestLine());

		return execute(client, httppost, callback);
	}

	public Future<byte[]> get(String uri, Callback<byte[]> callback) {
		try {
			HttpGet req = new HttpGet(uri);

			Log.debug("Starting HTTP GET request", "request", req.getRequestLine());

			return execute(client, req, callback);
		} catch (Throwable e) {
			throw U.rte(e);
		}
	}

	private Future<byte[]> execute(CloseableHttpAsyncClient client, HttpRequestBase req, Callback<byte[]> callback) {

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
				.setConnectionRequestTimeout(5000).build();
		req.setConfig(requestConfig);

		Promise<byte[]> promise = Promises.create();

		FutureCallback<HttpResponse> cb = callback(callback, promise);
		client.execute(req, cb);

		return promise;
	}

	private <T> FutureCallback<HttpResponse> callback(final Callback<byte[]> callback, final Callback<byte[]> promise) {
		return new FutureCallback<HttpResponse>() {

			@Override
			public void completed(HttpResponse response) {
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode != 200) {
					Callbacks.error(callback, new HttpException(statusCode));
					Callbacks.error(promise, new HttpException(statusCode));
					return;
				}

				byte[] bytes;
				try {
					InputStream resp = response.getEntity().getContent();
					bytes = IOUtils.toByteArray(resp);
				} catch (Exception e) {
					Callbacks.error(callback, e);
					Callbacks.error(promise, e);
					return;
				}

				Callbacks.success(callback, bytes);
				Callbacks.success(promise, bytes);
			}

			@Override
			public void failed(Exception e) {
				Callbacks.error(callback, e);
				Callbacks.error(promise, e);
			}

			@Override
			public void cancelled() {
				Callbacks.error(callback, U.cancelled());
				Callbacks.error(promise, U.cancelled());
			}
		};
	}

}