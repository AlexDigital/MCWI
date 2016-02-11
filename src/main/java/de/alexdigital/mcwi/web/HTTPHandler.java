package de.alexdigital.mcwi.web;

import com.google.common.base.Joiner;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class HTTPHandler {

    private ChannelHandlerContext ctx;

    public HTTPHandler(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Getter
    private HTTPResult result = new HTTPResult();

    public void parseLine(String line) {
        this.result = this.parseOne(line, result);
    }


    public HTTPResult parse(String[] lines) {
        HTTPResult result = new HTTPResult();
        for (String line : lines) {
            result = this.parseOne(line, result);
        }
        return result;
    }

    private HTTPResult parseOne(String line, HTTPResult result) {
        List<String> splitted = Arrays.asList(line.split(" "));
        switch (splitted.get(0)) {
            case "GET":
            case "POST":
                result.setMethod(splitted.get(0));
                result.setPath(splitted.get(1));
                result.setHttpVersion(splitted.get(2));
                break;
            case "Host:":
                result.setHost(splitted.get(1));
                break;
            case "User-Agent:":
                result.setHost(Joiner.on(' ').join(splitted.subList(1, splitted.size() - 1)));
                break;
        }
        return result;
    }

    public void sendText(String contentType, String text) {
        byte[] bytes = text.getBytes(Charset.forName("UTF-8"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss z");
        String stringBuilder = "HTTP/1.1 200 OK\n" +
                "Date: " + simpleDateFormat.format(Calendar.getInstance().getTime()) + "\n" +
                "Server: MCWI Server - Netty 4\n" +
                "Last-Modified: " + simpleDateFormat.format(Calendar.getInstance().getTime()) +
                "Content-Type: " + contentType + "; charset=UTF-8\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Accept-Ranges: bytes\n" +
                "Connection: close\n\n" +
                text;

        ctx.writeAndFlush(stringBuilder);
    }

    public void send404() {
        String text = "<html><head><title>Not Found</title></head><body>The item was not found.<br><b>MCWI Server - based on Netty 4</b></body></html>";
        String stringBuilder = "HTTP/1.1 404 Not Found\n" +
                "Content-type: text/html\n" +
                "Content-length: " + text.getBytes().length + "\n\n" +
                text;

        ctx.writeAndFlush(stringBuilder);
    }

}
