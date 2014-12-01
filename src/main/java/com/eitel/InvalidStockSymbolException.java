package com.eitel;

class InvalidStockSymbolException extends IllegalArgumentException {
    private static final long serialVersionUID = -1658594682194644206L;

    public InvalidStockSymbolException(Exception e) {
        super(e);
    }
}
