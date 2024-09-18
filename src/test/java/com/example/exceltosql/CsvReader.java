package com.example.exceltosql;

/**
 * @author :sunjian23
 * @date : 2024/4/8 9:25
 */

import lombok.Data;

import java.io.*;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.HashMap;


public class CsvReader {
    private Reader inputStream = null;

    private String fileName = null;

    private UserSettings userSettings = new UserSettings();

    private Charset charset = null;

    private boolean useCustomRecordDelimiter = false;

    private DataBuffer dataBuffer = new DataBuffer();

    private ColumnBuffer columnBuffer = new ColumnBuffer();

    private RawRecordBuffer rawBuffer = new RawRecordBuffer();

    private boolean[] isQualified = null;

    private String rawRecord = "";

    private HeadersHolder headersHolder = new HeadersHolder();

    private boolean startedColumn = false;

    private boolean startedWithQualifier = false;

    private boolean hasMoreData = true;

    private char lastLetter = Character.MIN_VALUE;

    private boolean hasReadNextLine = false;

    private int columnsCount = 0;

    private long currentRecord = 0L;

    private String[] values = new String[10];

    private boolean initialized = false;

    private boolean closed = false;

    public CsvReader(String paramString, char paramChar, Charset paramCharset) throws FileNotFoundException {
        if (paramString == null)
            throw new IllegalArgumentException("Parameter fileName can not be null.");
        if (paramCharset == null)
            throw new IllegalArgumentException("Parameter charset can not be null.");
        if (!(new File(paramString)).exists())
            throw new FileNotFoundException("File " + paramString + " does not exist.");
        this.fileName = paramString;
        this.userSettings.Delimiter = paramChar;
        this.charset = paramCharset;
        this.isQualified = new boolean[this.values.length];
    }

    public CsvReader(String paramString, char paramChar) throws FileNotFoundException {
        this(paramString, paramChar, Charset.forName("ISO-8859-1"));
    }

    public CsvReader(String paramString) throws FileNotFoundException {
        this(paramString, ',');
    }

    public CsvReader(Reader paramReader, char paramChar) {
        if (paramReader == null)
            throw new IllegalArgumentException("Parameter inputStream can not be null.");
        this.inputStream = paramReader;
        this.userSettings.Delimiter = paramChar;
        this.initialized = true;
        this.isQualified = new boolean[this.values.length];
    }

    public CsvReader(Reader paramReader) {
        this(paramReader, ',');
    }

    public CsvReader(InputStream paramInputStream, char paramChar, Charset paramCharset) {
        this(new InputStreamReader(paramInputStream, paramCharset), paramChar);
    }

    public CsvReader(InputStream paramInputStream, Charset paramCharset) {
        this(new InputStreamReader(paramInputStream, paramCharset));
    }

//    public boolean getCaptureRawRecord() {
//        return this.userSettings.CaptureRawRecord;
//    }

    public void setCaptureRawRecord(boolean paramBoolean) {
        this.userSettings.CaptureRawRecord = paramBoolean;
    }

    public String getRawRecord() {
        return this.rawRecord;
    }

    public boolean getTrimWhitespace() {
        return this.userSettings.TrimWhitespace;
    }

    public void setTrimWhitespace(boolean paramBoolean) {
        this.userSettings.TrimWhitespace = paramBoolean;
    }

    public char getDelimiter() {
        return this.userSettings.Delimiter;
    }

    public void setDelimiter(char paramChar) {
        this.userSettings.Delimiter = paramChar;
    }

    public char getRecordDelimiter() {
        return this.userSettings.RecordDelimiter;
    }

    public void setRecordDelimiter(char paramChar) {
        this.useCustomRecordDelimiter = true;
        this.userSettings.RecordDelimiter = paramChar;
    }

    public char getTextQualifier() {
        return this.userSettings.TextQualifier;
    }

    public void setTextQualifier(char paramChar) {
        this.userSettings.TextQualifier = paramChar;
    }

    public boolean getUseTextQualifier() {
        return this.userSettings.UseTextQualifier;
    }

    public void setUseTextQualifier(boolean paramBoolean) {
        this.userSettings.UseTextQualifier = paramBoolean;
    }

    public char getComment() {
        return this.userSettings.Comment;
    }

    public void setComment(char paramChar) {
        this.userSettings.Comment = paramChar;
    }

    public boolean getUseComments() {
        return this.userSettings.UseComments;
    }

    public void setUseComments(boolean paramBoolean) {
        this.userSettings.UseComments = paramBoolean;
    }

    public int getEscapeMode() {
        return this.userSettings.EscapeMode;
    }

    public void setEscapeMode(int paramInt) throws IllegalArgumentException {
        if (paramInt != 1 && paramInt != 2)
            throw new IllegalArgumentException("Parameter escapeMode must be a valid value.");
        this.userSettings.EscapeMode = paramInt;
    }

    public boolean getSkipEmptyRecords() {
        return this.userSettings.SkipEmptyRecords;
    }

    public void setSkipEmptyRecords(boolean paramBoolean) {
        this.userSettings.SkipEmptyRecords = paramBoolean;
    }

    public boolean getSafetySwitch() {
        return this.userSettings.SafetySwitch;
    }

    public void setSafetySwitch(boolean paramBoolean) {
        this.userSettings.SafetySwitch = paramBoolean;
    }

    public int getColumnCount() {
        return this.columnsCount;
    }

    public long getCurrentRecord() {
        return this.currentRecord - 1L;
    }

    public int getHeaderCount() {
        return this.headersHolder.Length;
    }

    public String[] getHeaders() throws IOException {
        checkClosed();
        if (this.headersHolder.Headers == null)
            return null;
        String[] arrayOfString = new String[this.headersHolder.Length];
        System.arraycopy(this.headersHolder.Headers, 0, arrayOfString, 0, this.headersHolder.Length);
        return arrayOfString;
    }

    public void setHeaders(String[] paramArrayOfString) {
        this.headersHolder.Headers = paramArrayOfString;
        this.headersHolder.IndexByName.clear();
        if (paramArrayOfString != null) {
            this.headersHolder.Length = paramArrayOfString.length;
        } else {
            this.headersHolder.Length = 0;
        }
        for (byte b = 0; b < this.headersHolder.Length; b++)
            this.headersHolder.IndexByName.put(paramArrayOfString[b], new Integer(b));
    }

    public String[] getValues() throws IOException {
        checkClosed();
        String[] arrayOfString = new String[this.columnsCount];
        System.arraycopy(this.values, 0, arrayOfString, 0, this.columnsCount);
        return arrayOfString;
    }

    public String get(int paramInt) throws IOException {
        checkClosed();
        return (paramInt > -1 && paramInt < this.columnsCount) ? this.values[paramInt] : "";
    }

    public String get(String paramString) throws IOException {
        checkClosed();
        return get(getIndex(paramString));
    }

    public static CsvReader parse(String paramString) {
        if (paramString == null)
            throw new IllegalArgumentException("Parameter data can not be null.");
        return new CsvReader(new StringReader(paramString));
    }

    public boolean readRecord() throws IOException {
        checkClosed();
        this.columnsCount = 0;
        this.rawBuffer.Position = 0;
        this.dataBuffer.LineStart = this.dataBuffer.Position;
        this.hasReadNextLine = false;
        if (this.hasMoreData) {
            do {
                if (this.dataBuffer.Position == this.dataBuffer.Count) {
                    checkDataLength();
                } else {
                    this.startedWithQualifier = false;
                    char c = this.dataBuffer.Buffer[this.dataBuffer.Position];
                    if (this.userSettings.UseTextQualifier && c == this.userSettings.TextQualifier) {
                        this.lastLetter = c;
                        this.startedColumn = true;
                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                        this.startedWithQualifier = true;
                        boolean bool1 = false;
                        char c1 = this.userSettings.TextQualifier;
                        if (this.userSettings.EscapeMode == 2)
                            c1 = '\\';
                        boolean bool2 = false;
                        boolean bool3 = false;
                        boolean bool4 = false;
                        byte b1 = 1;
                        byte b2 = 0;
                        char c2 = Character.MIN_VALUE;
                        this.dataBuffer.Position++;
                        do {
                            if (this.dataBuffer.Position == this.dataBuffer.Count) {
                                checkDataLength();
                            } else {
                                c = this.dataBuffer.Buffer[this.dataBuffer.Position];
                                if (bool2) {
                                    this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                    if (c == this.userSettings.Delimiter) {
                                        endColumn();
                                    } else if ((!this.useCustomRecordDelimiter && (c == '\r' || c == '\n')) || (this.useCustomRecordDelimiter && c == this.userSettings.RecordDelimiter)) {
                                        endColumn();
                                        endRecord();
                                    }
                                } else if (bool4) {
                                    b2++;
                                    switch (b1) {
                                        case 1:
                                            c2 = (char) (c2 * 16);
                                            c2 = (char) (c2 + hexToDec(c));
                                            if (b2 == 4)
                                                bool4 = false;
                                            break;
                                        case 2:
                                            c2 = (char) (c2 * 8);
                                            c2 = (char) (c2 + (char) (c - 48));
                                            if (b2 == 3)
                                                bool4 = false;
                                            break;
                                        case 3:
                                            c2 = (char) (c2 * 10);
                                            c2 = (char) (c2 + (char) (c - 48));
                                            if (b2 == 3)
                                                bool4 = false;
                                            break;
                                        case 4:
                                            c2 = (char) (c2 * 16);
                                            c2 = (char) (c2 + hexToDec(c));
                                            if (b2 == 2)
                                                bool4 = false;
                                            break;
                                    }
                                    if (!bool4) {
                                        appendLetter(c2);
                                    } else {
                                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                    }
                                } else if (c == this.userSettings.TextQualifier) {
                                    if (bool3) {
                                        bool3 = false;
                                        bool1 = false;
                                    } else {
                                        updateCurrentValue();
                                        if (this.userSettings.EscapeMode == 1)
                                            bool3 = true;
                                        bool1 = true;
                                    }
                                } else if (this.userSettings.EscapeMode == 2 && bool3) {
                                    switch (c) {
                                        case 'n':
                                            appendLetter('\n');
                                            break;
                                        case 'r':
                                            appendLetter('\r');
                                            break;
                                        case 't':
                                            appendLetter('\t');
                                            break;
                                        case 'b':
                                            appendLetter('\b');
                                            break;
                                        case 'f':
                                            appendLetter('\f');
                                            break;
                                        case 'e':
                                            appendLetter('\033');
                                            break;
                                        case 'v':
                                            appendLetter('\013');
                                            break;
                                        case 'a':
                                            appendLetter('\007');
                                            break;
                                        case '0':
                                        case '1':
                                        case '2':
                                        case '3':
                                        case '4':
                                        case '5':
                                        case '6':
                                        case '7':
                                            b1 = 2;
                                            bool4 = true;
                                            b2 = 1;
                                            c2 = (char) (c - 48);
                                            this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                            break;
                                        case 'D':
                                        case 'O':
                                        case 'U':
                                        case 'X':
                                        case 'd':
                                        case 'o':
                                        case 'u':
                                        case 'x':
                                            switch (c) {
                                                case 'U':
                                                case 'u':
                                                    b1 = 1;
                                                    break;
                                                case 'X':
                                                case 'x':
                                                    b1 = 4;
                                                    break;
                                                case 'O':
                                                case 'o':
                                                    b1 = 2;
                                                    break;
                                                case 'D':
                                                case 'd':
                                                    b1 = 3;
                                                    break;
                                            }
                                            bool4 = true;
                                            b2 = 0;
                                            c2 = Character.MIN_VALUE;
                                            this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                            break;
                                    }
                                    bool3 = false;
                                } else if (c == c1) {
                                    updateCurrentValue();
                                    bool3 = true;
                                } else if (bool1) {
                                    if (c == this.userSettings.Delimiter) {
                                        endColumn();
                                    } else if ((!this.useCustomRecordDelimiter && (c == '\r' || c == '\n')) || (this.useCustomRecordDelimiter && c == this.userSettings.RecordDelimiter)) {
                                        endColumn();
                                        endRecord();
                                    } else {
                                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                        bool2 = true;
                                    }
                                    bool1 = false;
                                }
                                this.lastLetter = c;
                                if (this.startedColumn) {
                                    this.dataBuffer.Position++;
                                    if (this.userSettings.SafetySwitch && this.dataBuffer.Position - this.dataBuffer.ColumnStart + this.columnBuffer.Position > 100000) {
                                        close();
                                        throw new IOException("Maximum column length of 100,000 exceeded in column " + NumberFormat.getIntegerInstance().format(this.columnsCount) + " in record " + NumberFormat.getIntegerInstance().format(this.currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting column lengths greater than 100,000 characters to" + " avoid this error.");
                                    }
                                }
                            }
                        } while (this.hasMoreData && this.startedColumn);
                    } else if (c == this.userSettings.Delimiter) {
                        this.lastLetter = c;
                        endColumn();
                    } else if (this.useCustomRecordDelimiter && c == this.userSettings.RecordDelimiter) {
                        if (this.startedColumn || this.columnsCount > 0 || !this.userSettings.SkipEmptyRecords) {
                            endColumn();
                            endRecord();
                        } else {
                            this.dataBuffer.LineStart = this.dataBuffer.Position + 1;
                        }
                        this.lastLetter = c;
                    } else if (!this.useCustomRecordDelimiter && (c == '\r' || c == '\n')) {
                        if (this.startedColumn || this.columnsCount > 0 || (!this.userSettings.SkipEmptyRecords && (c == '\r' || this.lastLetter != '\r'))) {
                            endColumn();
                            endRecord();
                        } else {
                            this.dataBuffer.LineStart = this.dataBuffer.Position + 1;
                        }
                        this.lastLetter = c;
                    } else if (this.userSettings.UseComments && this.columnsCount == 0 && c == this.userSettings.Comment) {
                        this.lastLetter = c;
                        skipLine();
                    } else if (this.userSettings.TrimWhitespace && (c == ' ' || c == '\t')) {
                        this.startedColumn = true;
                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                    } else {
                        this.startedColumn = true;
                        this.dataBuffer.ColumnStart = this.dataBuffer.Position;
                        boolean bool1 = false;
                        boolean bool2 = false;
                        byte b1 = 1;
                        byte b2 = 0;
                        char c1 = Character.MIN_VALUE;
                        boolean bool3 = true;
                        do {
                            if (!bool3 && this.dataBuffer.Position == this.dataBuffer.Count) {
                                checkDataLength();
                            } else {
                                if (!bool3)
                                    c = this.dataBuffer.Buffer[this.dataBuffer.Position];
                                if (!this.userSettings.UseTextQualifier && this.userSettings.EscapeMode == 2 && c == '\\') {
                                    if (bool1) {
                                        bool1 = false;
                                    } else {
                                        updateCurrentValue();
                                        bool1 = true;
                                    }
                                } else if (bool2) {
                                    b2++;
                                    switch (b1) {
                                        case 1:
                                            c1 = (char) (c1 * 16);
                                            c1 = (char) (c1 + hexToDec(c));
                                            if (b2 == 4)
                                                bool2 = false;
                                            break;
                                        case 2:
                                            c1 = (char) (c1 * 8);
                                            c1 = (char) (c1 + (char) (c - 48));
                                            if (b2 == 3)
                                                bool2 = false;
                                            break;
                                        case 3:
                                            c1 = (char) (c1 * 10);
                                            c1 = (char) (c1 + (char) (c - 48));
                                            if (b2 == 3)
                                                bool2 = false;
                                            break;
                                        case 4:
                                            c1 = (char) (c1 * 16);
                                            c1 = (char) (c1 + hexToDec(c));
                                            if (b2 == 2)
                                                bool2 = false;
                                            break;
                                    }
                                    if (!bool2) {
                                        appendLetter(c1);
                                    } else {
                                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                    }
                                } else if (this.userSettings.EscapeMode == 2 && bool1) {
                                    switch (c) {
                                        case 'n':
                                            appendLetter('\n');
                                            break;
                                        case 'r':
                                            appendLetter('\r');
                                            break;
                                        case 't':
                                            appendLetter('\t');
                                            break;
                                        case 'b':
                                            appendLetter('\b');
                                            break;
                                        case 'f':
                                            appendLetter('\f');
                                            break;
                                        case 'e':
                                            appendLetter('\033');
                                            break;
                                        case 'v':
                                            appendLetter('\013');
                                            break;
                                        case 'a':
                                            appendLetter('\007');
                                            break;
                                        case '0':
                                        case '1':
                                        case '2':
                                        case '3':
                                        case '4':
                                        case '5':
                                        case '6':
                                        case '7':
                                            b1 = 2;
                                            bool2 = true;
                                            b2 = 1;
                                            c1 = (char) (c - 48);
                                            this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                            break;
                                        case 'D':
                                        case 'O':
                                        case 'U':
                                        case 'X':
                                        case 'd':
                                        case 'o':
                                        case 'u':
                                        case 'x':
                                            switch (c) {
                                                case 'U':
                                                case 'u':
                                                    b1 = 1;
                                                    break;
                                                case 'X':
                                                case 'x':
                                                    b1 = 4;
                                                    break;
                                                case 'O':
                                                case 'o':
                                                    b1 = 2;
                                                    break;
                                                case 'D':
                                                case 'd':
                                                    b1 = 3;
                                                    break;
                                            }
                                            bool2 = true;
                                            b2 = 0;
                                            c1 = Character.MIN_VALUE;
                                            this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                            break;
                                    }
                                    bool1 = false;
                                } else if (c == this.userSettings.Delimiter) {
                                    endColumn();
                                } else if ((!this.useCustomRecordDelimiter && (c == '\r' || c == '\n')) || (this.useCustomRecordDelimiter && c == this.userSettings.RecordDelimiter)) {
                                    endColumn();
                                    endRecord();
                                }
                                this.lastLetter = c;
                                bool3 = false;
                                if (this.startedColumn) {
                                    this.dataBuffer.Position++;
                                    if (this.userSettings.SafetySwitch && this.dataBuffer.Position - this.dataBuffer.ColumnStart + this.columnBuffer.Position > 100000) {
                                        close();
                                        throw new IOException("Maximum column length of 100,000 exceeded in column " + NumberFormat.getIntegerInstance().format(this.columnsCount) + " in record " + NumberFormat.getIntegerInstance().format(this.currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting column lengths greater than 100,000 characters to" + " avoid this error.");
                                    }
                                }
                            }
                        } while (this.hasMoreData && this.startedColumn);
                    }
                    if (this.hasMoreData)
                        this.dataBuffer.Position++;
                }
            } while (this.hasMoreData && !this.hasReadNextLine);
            if (this.startedColumn || this.lastLetter == this.userSettings.Delimiter) {
                endColumn();
                endRecord();
            }
        }
        if (this.userSettings.CaptureRawRecord) {
            if (this.hasMoreData) {
                if (this.rawBuffer.Position == 0) {
                    this.rawRecord = new String(this.dataBuffer.Buffer, this.dataBuffer.LineStart, this.dataBuffer.Position - this.dataBuffer.LineStart - 1);
                } else {
                    this.rawRecord = new String(this.rawBuffer.Buffer, 0, this.rawBuffer.Position) + new String(this.dataBuffer.Buffer, this.dataBuffer.LineStart, this.dataBuffer.Position - this.dataBuffer.LineStart - 1);
                }
            } else {
                this.rawRecord = new String(this.rawBuffer.Buffer, 0, this.rawBuffer.Position);
            }
        } else {
            this.rawRecord = "";
        }
        return this.hasReadNextLine;
    }

    private void checkDataLength() throws IOException {
        if (!this.initialized) {
            if (this.fileName != null)
                this.inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileName), this.charset), 4096);
            this.charset = null;
            this.initialized = true;
        }
        updateCurrentValue();
        if (this.userSettings.CaptureRawRecord && this.dataBuffer.Count > 0) {
            if (this.rawBuffer.Buffer.length - this.rawBuffer.Position < this.dataBuffer.Count - this.dataBuffer.LineStart) {
                int i = this.rawBuffer.Buffer.length + Math.max(this.dataBuffer.Count - this.dataBuffer.LineStart, this.rawBuffer.Buffer.length);
                char[] arrayOfChar = new char[i];
                System.arraycopy(this.rawBuffer.Buffer, 0, arrayOfChar, 0, this.rawBuffer.Position);
                this.rawBuffer.Buffer = arrayOfChar;
            }
            System.arraycopy(this.dataBuffer.Buffer, this.dataBuffer.LineStart, this.rawBuffer.Buffer, this.rawBuffer.Position, this.dataBuffer.Count - this.dataBuffer.LineStart);
            this.rawBuffer.Position += this.dataBuffer.Count - this.dataBuffer.LineStart;
        }
        try {
            this.dataBuffer.Count = this.inputStream.read(this.dataBuffer.Buffer, 0, this.dataBuffer.Buffer.length);
        } catch (IOException iOException) {
            close();
            throw iOException;
        }
        if (this.dataBuffer.Count == -1)
            this.hasMoreData = false;
        this.dataBuffer.Position = 0;
        this.dataBuffer.LineStart = 0;
        this.dataBuffer.ColumnStart = 0;
    }

    public boolean readHeaders() throws IOException {
        boolean bool = readRecord();
        this.headersHolder.Length = this.columnsCount;
        this.headersHolder.Headers = new String[this.columnsCount];
        for (byte b = 0; b < this.headersHolder.Length; b++) {
            String str = get(b);
            this.headersHolder.Headers[b] = str;
            this.headersHolder.IndexByName.put(str, new Integer(b));
        }
        if (bool)
            this.currentRecord--;
        this.columnsCount = 0;
        return bool;
    }

    public String getHeader(int paramInt) throws IOException {
        checkClosed();
        return (paramInt > -1 && paramInt < this.headersHolder.Length) ? this.headersHolder.Headers[paramInt] : "";
    }

    public boolean isQualified(int paramInt) throws IOException {
        checkClosed();
        return (paramInt < this.columnsCount && paramInt > -1) ? this.isQualified[paramInt] : false;
    }

    private void endColumn() throws IOException {
        String str = "";
        if (this.startedColumn)
            if (this.columnBuffer.Position == 0) {
                if (this.dataBuffer.ColumnStart < this.dataBuffer.Position) {
                    int i = this.dataBuffer.Position - 1;
                    if (this.userSettings.TrimWhitespace && !this.startedWithQualifier)
                        while (i >= this.dataBuffer.ColumnStart && (this.dataBuffer.Buffer[i] == ' ' || this.dataBuffer.Buffer[i] == '\t'))
                            i--;
                    str = new String(this.dataBuffer.Buffer, this.dataBuffer.ColumnStart, i - this.dataBuffer.ColumnStart + 1);
                }
            } else {
                updateCurrentValue();
                int i = this.columnBuffer.Position - 1;
                if (this.userSettings.TrimWhitespace && !this.startedWithQualifier)
                    while (i >= 0 && (this.columnBuffer.Buffer[i] == ' ' || this.columnBuffer.Buffer[i] == ' '))
                        i--;
                str = new String(this.columnBuffer.Buffer, 0, i + 1);
            }
        this.columnBuffer.Position = 0;
        this.startedColumn = false;
        if (this.columnsCount >= 100000 && this.userSettings.SafetySwitch) {
            close();
            throw new IOException("Maximum column count of 100,000 exceeded in record " + NumberFormat.getIntegerInstance().format(this.currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting more than 100,000 columns per record to" + " avoid this error.");
        }
        if (this.columnsCount == this.values.length) {
            int i = this.values.length * 2;
            String[] arrayOfString = new String[i];
            System.arraycopy(this.values, 0, arrayOfString, 0, this.values.length);
            this.values = arrayOfString;
            boolean[] arrayOfBoolean = new boolean[i];
            System.arraycopy(this.isQualified, 0, arrayOfBoolean, 0, this.isQualified.length);
            this.isQualified = arrayOfBoolean;
        }
        this.values[this.columnsCount] = str;
        this.isQualified[this.columnsCount] = this.startedWithQualifier;
        str = "";
        this.columnsCount++;
    }

    private void appendLetter(char paramChar) {
        if (this.columnBuffer.Position == this.columnBuffer.Buffer.length) {
            int i = this.columnBuffer.Buffer.length * 2;
            char[] arrayOfChar = new char[i];
            System.arraycopy(this.columnBuffer.Buffer, 0, arrayOfChar, 0, this.columnBuffer.Position);
            this.columnBuffer.Buffer = arrayOfChar;
        }
        this.columnBuffer.Buffer[this.columnBuffer.Position++] = paramChar;
        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
    }

    private void updateCurrentValue() {
        if (this.startedColumn && this.dataBuffer.ColumnStart < this.dataBuffer.Position) {
            if (this.columnBuffer.Buffer.length - this.columnBuffer.Position < this.dataBuffer.Position - this.dataBuffer.ColumnStart) {
                int i = this.columnBuffer.Buffer.length + Math.max(this.dataBuffer.Position - this.dataBuffer.ColumnStart, this.columnBuffer.Buffer.length);
                char[] arrayOfChar = new char[i];
                System.arraycopy(this.columnBuffer.Buffer, 0, arrayOfChar, 0, this.columnBuffer.Position);
                this.columnBuffer.Buffer = arrayOfChar;
            }
            System.arraycopy(this.dataBuffer.Buffer, this.dataBuffer.ColumnStart, this.columnBuffer.Buffer, this.columnBuffer.Position, this.dataBuffer.Position - this.dataBuffer.ColumnStart);
            this.columnBuffer.Position += this.dataBuffer.Position - this.dataBuffer.ColumnStart;
        }
        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
    }

    private void endRecord() throws IOException {
        this.hasReadNextLine = true;
        this.currentRecord++;
    }

    public int getIndex(String paramString) throws IOException {
        checkClosed();
        Object object = this.headersHolder.IndexByName.get(paramString);
        return (object != null) ? ((Integer) object).intValue() : -1;
    }

    public boolean skipRecord() throws IOException {
        checkClosed();
        boolean bool = false;
        if (this.hasMoreData) {
            bool = readRecord();
            if (bool)
                this.currentRecord--;
        }
        return bool;
    }

    public boolean skipLine() throws IOException {
        checkClosed();
        this.columnsCount = 0;
        boolean bool = false;
        if (this.hasMoreData) {
            boolean bool1 = false;
            do {
                if (this.dataBuffer.Position == this.dataBuffer.Count) {
                    checkDataLength();
                } else {
                    bool = true;
                    char c = this.dataBuffer.Buffer[this.dataBuffer.Position];
                    if (c == '\r' || c == '\n')
                        bool1 = true;
                    this.lastLetter = c;
                    if (!bool1)
                        this.dataBuffer.Position++;
                }
            } while (this.hasMoreData && !bool1);
            this.columnBuffer.Position = 0;
            this.dataBuffer.LineStart = this.dataBuffer.Position + 1;
        }
        this.rawBuffer.Position = 0;
        this.rawRecord = "";
        return bool;
    }

    public void close() {
        if (!this.closed) {
            close(true);
            this.closed = true;
        }
    }

    private void close(boolean paramBoolean) {
        if (!this.closed) {
            if (paramBoolean) {
                this.charset = null;
                this.headersHolder.Headers = null;
                this.headersHolder.IndexByName = null;
                this.dataBuffer.Buffer = null;
                this.columnBuffer.Buffer = null;
                this.rawBuffer.Buffer = null;
            }
            try {
                if (this.initialized)
                    this.inputStream.close();
            } catch (Exception exception) {
            }
            this.inputStream = null;
            this.closed = true;
        }
    }

    private void checkClosed() throws IOException {
        if (this.closed)
            throw new IOException("This instance of the CsvReader class has already been closed.");
    }

    protected void finalize() {
        close(false);
    }

    private static char hexToDec(char paramChar) {
        char c;
        if (paramChar >= 'a') {
            c = (char) (paramChar - 97 + 10);
        } else if (paramChar >= 'A') {
            c = (char) (paramChar - 65 + 10);
        } else {
            c = (char) (paramChar - 48);
        }
        return c;
    }


    private class HeadersHolder {
        public String[] Headers = null;

        public int Length = 0;

        public HashMap IndexByName = new HashMap<Object, Object>();
    }

    @Data
    private static class UserSettings {
        public boolean CaseSensitive = true;

        public char TextQualifier = '"';

        public boolean TrimWhitespace = true;

        public boolean UseTextQualifier = true;

        public char Delimiter = ',';

        public char RecordDelimiter = Character.MIN_VALUE;

        public char Comment = '#';

        public boolean UseComments = false;

        public int EscapeMode = 1;

        public boolean SafetySwitch = true;

        public boolean SkipEmptyRecords = true;

        public boolean CaptureRawRecord = true;
    }

    private static class RawRecordBuffer {
        public char[] Buffer = new char[500];

        public int Position = 0;
    }

    private static class ColumnBuffer {
        public char[] Buffer = new char[50];

        public int Position = 0;
    }

    private static class DataBuffer {
        public char[] Buffer = new char[1024];

        public int Position = 0;

        public int Count = 0;

        public int ColumnStart = 0;

        public int LineStart = 0;
    }

}

