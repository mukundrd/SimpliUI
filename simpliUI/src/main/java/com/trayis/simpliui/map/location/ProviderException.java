package com.trayis.simpliui.map.location;

/**
 * Created by mukundrd on 6/9/17.
 */

public class ProviderException extends Throwable {

    String provider;

    public ProviderException(String provider, String detailMessage) {
        super(detailMessage);
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public String toString() {
        return super.toString() + " | ProviderException{ provider='" + provider + "\'}";
    }
}
