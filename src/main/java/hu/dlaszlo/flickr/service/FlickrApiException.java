package hu.dlaszlo.flickr.service;

import java.io.IOException;

/**
 * Created by dlasz on 2016. 03. 02..
 */
public class FlickrApiException extends Exception {
    private static final long serialVersionUID = 1L;

    public FlickrApiException(String message, IOException cause) {
        super(message, cause);
    }

    public FlickrApiException(String message) {
        super(message);
    }
}
