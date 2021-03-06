package com.arcao.geocaching.api.downloader;

import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration;
import com.arcao.geocaching.api.exception.InvalidResponseException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.parser.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Default implementation of {@link JsonDownloader} using {@link HttpURLConnection}
 *
 * @author arcao
 */
public class DefaultJsonDownloader implements JsonDownloader {
    private static final Logger logger = LoggerFactory.getLogger(DefaultJsonDownloader.class);
    private static final int HTTP_ERROR_400 = 400;
    private static final int BUFFER_SIZE = 8192;

    private final GeocachingApiConfiguration configuration;
    private boolean debug = false;

    /**
     * Create a new {@link DefaultJsonDownloader} using specified configuration
     *
     * @param configuration configuration
     */
    public DefaultJsonDownloader(GeocachingApiConfiguration configuration) {
        this.configuration = configuration;
    }

    public DefaultJsonDownloader debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public JsonReader get(URL url) throws NetworkException, InvalidResponseException {
        InputStream is;
        InputStreamReader isr;

        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // important! sometimes GC API takes too long to return response
            con.setConnectTimeout(configuration.getConnectTimeout());
            con.setReadTimeout(configuration.getReadTimeout());

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Java Geocaching API");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Accept-Language", "en-US");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate");

            is = con.getResponseCode() >= HTTP_ERROR_400 ? con.getErrorStream() : con.getInputStream();

            final String encoding = con.getContentEncoding();

            if (encoding != null && "gzip".equalsIgnoreCase(encoding)) {
                logger.debug("get: GZIP OK");
                is = new GZIPInputStream(is);
            } else if (encoding != null && "deflate".equalsIgnoreCase(encoding)) {
                logger.debug("get: DEFLATE OK");
                is = new InflaterInputStream(is, new Inflater(true));
            } else {
                logger.debug("get: WITHOUT COMPRESSION");
            }

            if (con.getResponseCode() >= HTTP_ERROR_400 || notJsonResponse(con)) {
                isr = new InputStreamReader(is, "UTF-8");

                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[BUFFER_SIZE];
                int len;

                while ((len = isr.read(buffer)) != -1) {
                    sb.append(buffer, 0, len);
                }

                isr.close();

                // read error response
                throw new InvalidResponseException(con.getResponseCode(), con.getResponseMessage(), sb.toString());
            }

            isr = new InputStreamReader(is, "UTF-8");

            if (debug)
                return new DebugJsonReader(isr);

            return new JsonReader(isr);
        } catch (InvalidResponseException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.toString(), e);
            throw new NetworkException("Error occurs while downloading data (" + e.getClass().getSimpleName() + ")", e);
        }
    }

    @Override
    public JsonReader post(URL url, byte[] postData) throws NetworkException, InvalidResponseException {
        InputStream is;
        InputStreamReader isr;

        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setDoOutput(true);

            // important! sometimes GC API takes too long to return response
            con.setConnectTimeout(configuration.getConnectTimeout());
            con.setReadTimeout(configuration.getReadTimeout());

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Content-Length", Integer.toString(postData.length));
            con.setRequestProperty("User-Agent", "Java Geocaching API");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Accept-Language", "en-US");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate");

            OutputStream os = con.getOutputStream();

            os.write(postData);
            os.flush();
            os.close();

            is = con.getResponseCode() >= HTTP_ERROR_400 ? con.getErrorStream() : con.getInputStream();

            final String encoding = con.getContentEncoding();

            if (encoding != null && "gzip".equalsIgnoreCase(encoding)) {
                logger.debug("callPost(): GZIP OK");
                is = new GZIPInputStream(is);
            } else if (encoding != null && "deflate".equalsIgnoreCase(encoding)) {
                logger.debug("callPost(): DEFLATE OK");
                is = new InflaterInputStream(is, new Inflater(true));
            } else {
                logger.debug("callPost(): WITHOUT COMPRESSION");
            }

            if (con.getResponseCode() >= HTTP_ERROR_400 || notJsonResponse(con)) {
                isr = new InputStreamReader(is, "UTF-8");

                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[BUFFER_SIZE];
                int len;

                while ((len = isr.read(buffer)) != -1) {
                    sb.append(buffer, 0, len);
                }

                isr.close();

                // read error response
                throw new InvalidResponseException(con.getResponseCode(), con.getResponseMessage(), sb.toString());
            }

            isr = new InputStreamReader(is, "UTF-8");

            if (debug)
                return new DebugJsonReader(isr);

            return new JsonReader(isr);
        } catch (InvalidResponseException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.toString(), e);
            throw new NetworkException("Error occurs while downloading data (" + e.getClass().getSimpleName() + "): " + e.getMessage(), e);
        }
    }

    private static boolean notJsonResponse(HttpURLConnection con) {
        String contentType = con.getHeaderField("Content-Type");

        return contentType == null || !contentType.toLowerCase(Locale.US).contains("/json");
    }

    private static class DebugJsonReader extends JsonReader {
        private static final Logger logger = LoggerFactory.getLogger(DebugJsonReader.class);

        DebugJsonReader(Reader in) throws IOException {
            super(writeOutput(in));
        }

        private static Reader writeOutput(Reader in) throws IOException {
            try {
                StringBuilder sb = new StringBuilder();

                char[] buffer = new char[BUFFER_SIZE];
                int len;

                while ((len = in.read(buffer)) > 0) {
                    sb.append(buffer, 0, len);
                }
                logger.debug(sb.toString());

                return new StringReader(sb.toString());
            } finally {
                if (in != null)
                    in.close();
            }
        }
    }
}
