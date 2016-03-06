package hu.dlaszlo.flickr.service;

/**
 * Created by dlasz on 2016. 03. 03..
 */
@FunctionalInterface
public interface FlickrApiCall<T> {

    T call() throws FlickrApiException;

}
