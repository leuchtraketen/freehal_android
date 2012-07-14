package net.freehal.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class HttpUtil {

	public static synchronized String executeHttpGet(String url)
			throws Exception {
		String page = "HTTP Error!";
		BufferedReader in = null;
		try {

			HttpClient client = getTrustAllHttpClient();
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			page = sb.toString();

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return page;
	}

	private static HttpClient getTrustAllHttpClient() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(),
				443));

		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
				new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		ClientConnectionManager cm = new SingleClientConnManager(params,
				schemeRegistry);
		return new DefaultHttpClient(cm, params);
	}

	static class EasySSLSocketFactory implements LayeredSocketFactory {

		private SSLContext sslcontext = null;

		private SSLContext createEasySSLContext() throws IOException {
			try {
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(null,
						new TrustManager[] { new EasyX509TrustManager(null) },
						null);
				return context;
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}

		private SSLContext getSSLContext() throws IOException {
			if (this.sslcontext == null) {
				this.sslcontext = createEasySSLContext();
			}
			return this.sslcontext;
		}

		/**
		 * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket,
		 *      java.lang.String, int, java.net.InetAddress, int,
		 *      org.apache.http.params.HttpParams)
		 */
		public Socket connectSocket(Socket sock, String host, int port,
				InetAddress localAddress, int localPort, HttpParams params)
				throws IOException, UnknownHostException,
				ConnectTimeoutException {
			int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
			int soTimeout = HttpConnectionParams.getSoTimeout(params);

			InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
			SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock
					: createSocket());

			if ((localAddress != null) || (localPort > 0)) {
				// we need to bind explicitly
				if (localPort < 0) {
					localPort = 0; // indicates "any"
				}
				InetSocketAddress isa = new InetSocketAddress(localAddress,
						localPort);
				sslsock.bind(isa);
			}

			sslsock.connect(remoteAddress, connTimeout);
			sslsock.setSoTimeout(soTimeout);
			return sslsock;

		}

		/**
		 * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
		 */
		public Socket createSocket() throws IOException {
			return getSSLContext().getSocketFactory().createSocket();
		}

		/**
		 * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
		 */
		public boolean isSecure(Socket socket) throws IllegalArgumentException {
			return true;
		}

		/**
		 * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket,
		 *      java.lang.String, int, boolean)
		 */
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return getSSLContext().getSocketFactory().createSocket(socket,
					host, port, autoClose);
		}

		// -------------------------------------------------------------------
		// javadoc in org.apache.http.conn.scheme.SocketFactory says :
		// Both Object.equals() and Object.hashCode() must be overridden
		// for the correct operation of some connection managers
		// -------------------------------------------------------------------

		public boolean equals(Object obj) {
			return ((obj != null) && obj.getClass().equals(
					EasySSLSocketFactory.class));
		}

		public int hashCode() {
			return EasySSLSocketFactory.class.hashCode();
		}

	}

	static class EasyX509TrustManager implements X509TrustManager {

		private X509TrustManager standardTrustManager = null;

		/**
		 * Constructor for EasyX509TrustManager.
		 */
		public EasyX509TrustManager(KeyStore keystore)
				throws NoSuchAlgorithmException, KeyStoreException {
			super();
			TrustManagerFactory factory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			factory.init(keystore);
			TrustManager[] trustmanagers = factory.getTrustManagers();
			if (trustmanagers.length == 0) {
				throw new NoSuchAlgorithmException("no trust manager found");
			}
			this.standardTrustManager = (X509TrustManager) trustmanagers[0];
		}

		/**
		 * @see 
		 *      javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate
		 *      [],String authType)
		 */
		public void checkClientTrusted(X509Certificate[] certificates,
				String authType) throws CertificateException {
			standardTrustManager.checkClientTrusted(certificates, authType);
		}

		/**
		 * @see 
		 *      javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate
		 *      [],String authType)
		 */
		public void checkServerTrusted(X509Certificate[] certificates,
				String authType) throws CertificateException {
			if ((certificates != null) && (certificates.length == 1)) {
				certificates[0].checkValidity();
			} else {
				standardTrustManager.checkServerTrusted(certificates, authType);
			}
		}

		/**
		 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
		 */
		public X509Certificate[] getAcceptedIssuers() {
			return this.standardTrustManager.getAcceptedIssuers();
		}

	}
}
