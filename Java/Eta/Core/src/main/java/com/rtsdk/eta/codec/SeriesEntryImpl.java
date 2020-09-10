package com.rtsdk.eta.codec;

import com.rtsdk.eta.codec.Buffer;
import com.rtsdk.eta.codec.CodecFactory;
import com.rtsdk.eta.codec.DecodeIterator;
import com.rtsdk.eta.codec.Decoders;
import com.rtsdk.eta.codec.EncodeIterator;
import com.rtsdk.eta.codec.SeriesEntry;

class SeriesEntryImpl implements SeriesEntry
{
    final Buffer _encodedData = CodecFactory.createBuffer();

    @Override
    public void clear()
    {
        _encodedData.clear();
    }

    @Override
    public int encode(EncodeIterator iter)
    {
        return Encoders.encodeSeriesEntry(iter, this);
    }

    @Override
    public int encodeInit(EncodeIterator iter, int maxEncodingSize)
    {
        return Encoders.encodeSeriesEntryInit(iter, this, maxEncodingSize);
    }

    @Override
    public int encodeComplete(EncodeIterator iter, boolean success)
    {
        return Encoders.encodeSeriesEntryComplete(iter, success);
    }

    @Override
    public int decode(DecodeIterator iter)
    {
        return Decoders.decodeSeriesEntry(iter, this);
    }

    @Override
    public Buffer encodedData()
    {
        return _encodedData;
    }

    @Override
    public void encodedData(Buffer encodedData)
    {
        assert (encodedData != null) : "encodedData must be non-null";

        ((BufferImpl)_encodedData).copyReferences(encodedData);
    }
}
