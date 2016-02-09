package de.alexdigital.mcwi.web;

import de.alexdigital.mcwi.McWebinterface;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class WebClientHandler extends SimpleChannelInboundHandler<String> {

    private HTTPHandler httpHandler;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.httpHandler = new HTTPHandler(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        if (s.isEmpty()) {
            HTTPResult result = httpHandler.getResult();
            if (result.getPath().equals("/")) {
                httpHandler.sendText("text/html", IOUtils.toString(McWebinterface.getInstance().getResource("web/index.html")));
            } else {
                InputStream inputStream = McWebinterface.getInstance().getResource("web" + result.getPath());
                if (inputStream != null) {
                    httpHandler.sendText(getMimeType(result.getPath(), inputStream), IOUtils.toString(inputStream));
                } else {
                    httpHandler.send404();
                }
            }
        } else {
            httpHandler.parseLine(s);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            return;
        }
    }

    private String getMimeType(String path, InputStream inputStream) throws IOException {
        String[] splittedPath = path.split("\\.");
        if (splittedPath.length > 1) {
            String ext = splittedPath[splittedPath.length - 1];
            switch (ext) {
                case "html":
                    return "text/html";
                case "css":
                    return "text/css";
                case "js":
                    return "text/javascript";
                default:
                    return URLConnection.guessContentTypeFromStream(inputStream);
            }
        } else {
            return URLConnection.guessContentTypeFromStream(inputStream);
        }
    }

}
