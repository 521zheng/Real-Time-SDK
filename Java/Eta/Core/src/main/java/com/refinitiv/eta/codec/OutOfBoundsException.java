package com.refinitiv.eta.codec;

import java.io.IOException;

class OutOfBoundsException extends IOException
{
    private static final long serialVersionUID = 1L;

    OutOfBoundsException(String message)
    {
        super(message);
    }

    OutOfBoundsException(Throwable cause)
    {
        initCause(cause);
    }

}
