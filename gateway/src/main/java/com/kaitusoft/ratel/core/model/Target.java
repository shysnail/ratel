package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.util.URLUtil;
import lombok.Data;
import lombok.ToString;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/2
 *          <p>
 *          write description here
 */
@Data
@ToString
public class Target {

    private String url;

    private int weight = 0;

    private int curWeight = 0;

    private boolean wildcard;

    private String[] wildcards;

    private boolean capture;

    private String[] captures;

    private String hostAndPort;

    private String host;

    private int port;

    public void setUrl(String url) {
        try {
            String tmpUrl = url;
            if(!url.startsWith("http://") ||url.startsWith("https://"))
                tmpUrl = "http://" + url;
            URL uri = new URL(tmpUrl);
            host = uri.getHost();
            port = uri.getPort();
            hostAndPort = host + (port == 80 || port <= 0 ? "" : (":" + port));
            this.url = url;
            analysis(uri.getPath());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }

    }

    private void analysis(String path) {
        Set<String> wildcards = URLUtil.getWildcard(path);
        setWildcard(wildcards.size() > 0);
        this.wildcards = new String[wildcards.size()];
        wildcards.toArray(this.wildcards);

        setCapture(URLUtil.isCapture(path));
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj instanceof Target){
            Target server = (Target)obj;

            return getUrl().equals(server.getUrl());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getUrl().hashCode();
    }

}
