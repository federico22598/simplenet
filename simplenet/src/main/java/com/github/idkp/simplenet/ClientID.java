package com.github.idkp.simplenet;

public final class ClientID {
    private final String privateIPAddress;
    private final String publicIPAddress;

    public ClientID(String privateIPAddress, String publicIPAddress) {
        this.privateIPAddress = privateIPAddress;
        this.publicIPAddress = publicIPAddress;
    }

    public String getPrivateIPAddress() {
        return privateIPAddress;
    }

    public String getPublicIPAddress() {
        return publicIPAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ClientID)) {
            return false;
        }

        ClientID other = (ClientID) obj;
        return other.privateIPAddress.equals(privateIPAddress)
                && other.publicIPAddress.equals(publicIPAddress);
    }

    @Override
    public int hashCode() {
        return 31 * (31 + privateIPAddress.hashCode()) + publicIPAddress.hashCode();
    }

    @Override
    public String toString() {
        return publicIPAddress + '-' + privateIPAddress;
    }
}
