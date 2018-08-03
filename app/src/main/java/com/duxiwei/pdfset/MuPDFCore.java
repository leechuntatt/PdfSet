package com.duxiwei.pdfset;

import android.graphics.Bitmap;
import android.graphics.PointF;

/**
 * Created by duxiwei on 18-8-1.
 * Mail  duxiwei@aliyun.com
 */
public class MuPDFCore{
    /* load our native library */
    static {
        System.loadLibrary("mupdf");
    }

    /* Readable members */
    private int numPages = -1;
    private float pageWidth;
    private float pageHeight;
    private long globals;
    private String file_format;
    private boolean isUnencryptedPDF;
    private final boolean wasOpenedFromBuffer;

    /* The native functions */
    private native long openFile(String filename);
    private native String fileFormatInternal();
    private native boolean isUnencryptedPDFInternal();
    private native int countPagesInternal();
    private native void gotoPageInternal(int localActionPageNum);
    private native float getPageWidth();
    private native float getPageHeight();
    private native void drawPage(Bitmap bitmap,
                                 int pageW, int pageH,
                                 int patchX, int patchY,
                                 int patchW, int patchH,
                                 long cookiePtr);
    private native void destroying();
    private native long createCookie();
    private native void destroyCookie(long cookie);
    private native void abortCookie(long cookie);

    public class Cookie
    {
        private final long cookiePtr;

        public Cookie()
        {
            cookiePtr = createCookie();
            if (cookiePtr == 0)
                throw new OutOfMemoryError();
        }

        public void abort()
        {
            abortCookie(cookiePtr);
        }

        public void destroy()
        {
            // We could do this in finalize, but there's no guarantee that
            // a finalize will occur before the muPDF context occurs.
            destroyCookie(cookiePtr);
        }
    }

    public MuPDFCore( String filename) throws Exception
    {
        globals = openFile(filename);
        file_format = fileFormatInternal();
        isUnencryptedPDF = isUnencryptedPDFInternal();
        wasOpenedFromBuffer = false;
    }


    public  int countPages()
    {
        if (numPages < 0)
            numPages = countPagesSynchronized();

        return numPages;
    }


    private synchronized int countPagesSynchronized() {
        return countPagesInternal();
    }

    /* Shim function */
    private void gotoPage(int page)
    {
        if (page > numPages-1)
            page = numPages-1;
        else if (page < 0)
            page = 0;
        gotoPageInternal(page);
        this.pageWidth = getPageWidth();
        this.pageHeight = getPageHeight();
    }

    public synchronized PointF getPageSize(int page) {
        gotoPage(page);
        return new PointF(pageWidth, pageHeight);
    }



    public synchronized void onDestroy() {
        destroying();
        globals = 0;
    }

    public synchronized void drawPage(Bitmap bm, int page,
                                      int pageW, int pageH,
                                      int patchX, int patchY,
                                      int patchW, int patchH,
                                      Cookie cookie) {
        gotoPage(page);
        drawPage(bm, pageW, pageH, patchX, patchY, patchW, patchH, cookie.cookiePtr);
    }

}
