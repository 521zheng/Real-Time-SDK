package com.rtsdk.eta.codec;

import com.rtsdk.eta.codec.Buffer;
import com.rtsdk.eta.codec.CodecFactory;
import com.rtsdk.eta.codec.DataTypes;
import com.rtsdk.eta.codec.Date;
import com.rtsdk.eta.codec.DateTime;
import com.rtsdk.eta.codec.DecodeIterator;
import com.rtsdk.eta.codec.EncodeIterator;
import com.rtsdk.eta.codec.Enum;
import com.rtsdk.eta.codec.FieldEntry;
import com.rtsdk.eta.codec.Int;
import com.rtsdk.eta.codec.Qos;
import com.rtsdk.eta.codec.Real;
import com.rtsdk.eta.codec.State;
import com.rtsdk.eta.codec.Time;
import com.rtsdk.eta.codec.UInt;

class FieldEntryImpl implements FieldEntry
{
    int         _fieldId;
    int         _dataType;
    final Buffer    _encodedData = CodecFactory.createBuffer();

    @Override
    public void clear()
    {
        _fieldId = 0;
        _dataType = 0;
        _encodedData.clear();
    }

    @Override
    public int encode(EncodeIterator iter)
    {
        return Encoders.encodeFieldEntry(iter, this, null);
    }
	
    @Override
    public int encodeBlank(EncodeIterator iter)
    {
        _encodedData.clear();

        return Encoders.encodeFieldEntry(iter, this, null);
    }

    @Override @Deprecated
    public int encode(EncodeIterator iter, Object data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, Int data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, UInt data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, Real data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, Date data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, Time data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, DateTime data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, Qos data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, State data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, Enum data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, Buffer data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, Float data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encode(EncodeIterator iter, Double data)
    {
        return Encoders.encodeFieldEntry(iter, this, data);
    }

    @Override
    public int encodeInit(EncodeIterator iter, int encodingMaxSize)
    {
        return Encoders.encodeFieldEntryInit(iter, this, encodingMaxSize);
    }

    @Override
    public int encodeComplete(EncodeIterator iter, boolean success)
    {
        return Encoders.encodeFieldEntryComplete(iter, success);
    }

    @Override
    public int decode(DecodeIterator iter)
    {
        return Decoders.decodeFieldEntry(iter, this);
    }

    @Override
    public void fieldId(int fieldId)
    {
        assert (fieldId >= -32768 && fieldId <= 32767) : "fieldId is out of range (-32768-32767)"; // int16

        _fieldId = fieldId;
    }

    @Override
    public int fieldId()
    {
        return _fieldId;
    }

    @Override
    public void dataType(int dataType)
    {
        assert (dataType > DataTypes.UNKNOWN && dataType <= DataTypes.LAST) : "dataType is out of range. Refer to DataTypes";

        _dataType = dataType;
    }

    @Override
    public int dataType()
    {
        return _dataType;
    }

    @Override
    public void encodedData(Buffer encodedData)
    {
        assert (encodedData != null) : "encodedData must be non-null";

        ((BufferImpl)_encodedData).copyReferences(encodedData);
    }

    @Override
    public Buffer encodedData()
    {
        return _encodedData;
    }
}
