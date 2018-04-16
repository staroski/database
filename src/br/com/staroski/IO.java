package br.com.staroski;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class IO {

    public static boolean readBoolean(DataInputStream in) {
        try {
            return in.readBoolean();
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static byte readByte(DataInputStream in) {
        try {
            return in.readByte();
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static char readChar(DataInputStream in) {
        try {
            return in.readChar();
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static double readDouble(DataInputStream in) {
        try {
            return in.readDouble();
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static float readFloat(DataInputStream in) {
        try {
            return in.readFloat();
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static int readInt(DataInputStream in) {
        try {
            return in.readInt();
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static long readLong(DataInputStream in) {
        try {
            return in.readLong();
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static short readShort(DataInputStream in) {
        try {
            return in.readShort();
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static String readString(DataInputStream in) {
        try {
            boolean notNull = in.readBoolean();
            if (notNull) {
                return in.readUTF();
            }
            return null;
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static void writeBoolean(DataOutputStream out, boolean value) {
        try {
            out.writeBoolean(value);
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static void writeByte(DataOutputStream out, byte value) {
        try {
            out.writeByte(value);
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static void writeChar(DataOutputStream out, char value) {
        try {
            out.writeChar(value);
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static void writeDouble(DataOutputStream out, double value) {
        try {
            out.writeDouble(value);
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static void writeFloat(DataOutputStream out, float value) {
        try {
            out.writeFloat(value);
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static void writeInt(DataOutputStream out, int value) {
        try {
            out.writeInt(value);
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static void writeLong(DataOutputStream out, long value) {
        try {
            out.writeLong(value);
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static void writeShort(DataOutputStream out, short value) {
        try {
            out.writeShort(value);
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public static void writeString(DataOutputStream out, String value) {
        try {
            boolean notNull = value != null;
            out.writeBoolean(notNull);
            if (notNull) {
                out.writeUTF(value);
            }
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    private IO() {}
}
