package com.uh.common.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class CRC64 {
    private static final long POLY32REV = 0xEDB88320;
    private static final long POLY64REV = 0x95AC9329AC4BC9B5L;
    private static long[] Table = new long[256];

    static {
        for (int i = 0; i < 256; i++) {
            long r = i;
            for (int j = 0; j < 8; j++)
                if ((r & 1) != 0)
                    r = (r >>> 1) ^ POLY64REV;
                else
                    r >>>= 1;
            Table[i] = r;
        }
    }

    /**
     * 给指定目录下的文件创建crc校验文件，目前是指向发版目录
     *
     * @param args
     */
    public static void main(String[] args) {

        System.out.println(new Date());

        String[] dirs = new String[]{"d:\\Documents\\MemDB\\TongRDS_eval\\新报价体系\\企业版\\安装文件\\绿色版"
                , "d:\\Documents\\MemDB\\TongRDS_eval\\新报价体系\\标准版\\安装文件\\绿色版"};
        for (String name : dirs) {
            try {
                File file = new File(name);
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    for (File f : files) {
                        if (f.isFile()) {
                            if (!f.getName().endsWith(".crc")) {
                                String crc = getFileCRC(f);
                                File crcFile = new File(f.getAbsolutePath() + ".crc");
                                try (OutputStream os = new FileOutputStream(crcFile)) {
                                    os.write(crc.getBytes(StandardCharsets.UTF_8));
                                }
                            }
                        }
                    }
                } else if (file.isFile()) {
                    if (!file.getName().endsWith(".crc")) {
                        String crc = getFileCRC(file);
                        File crcFile = new File(file.getAbsolutePath() + ".crc");
                        try (OutputStream os = new FileOutputStream(crcFile)) {
                            os.write(crc.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFileCRC(File file) throws IOException {
        long nResult = 0;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buff = new byte[8192];
            CRC64 crc64 = new CRC64();
            crc64.Init();
            while (true) {
                int nRead = fis.read(buff);
                if (nRead > 0) {
                    crc64.Update(buff, 0, nRead);
                }
                if (nRead < buff.length) {
                    break;
                }
            }
            nResult = crc64.GetDigest();
        }
        return Long.toHexString(nResult);
    }

    private long _value = -1;

    public void Init() {
        _value = -1;
    }

    public void Update(byte[] data) {
        Update(data, 0, data.length);
    }

    public void Update(byte[] data, int offset, int size) {
        for (int i = 0; i < size; i++) {
            UpdateByte(data[offset + i]);
//            _value = Table[((int) _value ^ data[offset + i]) & 0xFF] ^ (_value >>> 8);
        }
    }

    public void UpdateByte(int b) {
        _value = Table[((int) _value ^ b) & 0xFF] ^ (_value >>> 8);
    }

    public long GetDigest() {
        return _value ^ (-1);
    }

}

