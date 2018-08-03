package com.duxiwei.pdfset;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by duxiwei on 18-8-1.
 * Mail  duxiwei@aliyun.com
 */
public class PdfToPhoto {
    private static final String TAG = "PageView";
    private Point mParentSize; // Size of the view containing the pdf viewer. It could be the same as the screen if this view is full screen.
    protected Point mSize;   // Size of page at minimum zoom
    protected float mSourceScale;
    private Bitmap mEntireBm; // Bitmap used to draw the entire page at minimum zoom.
    private Bitmap mPatchBm; // Bitmap used to draw the zoomed image.
    private PointF pdfSize;
    protected MuPDFCore core;
    public PdfToPhoto() {
        try{
            this.core = new MuPDFCore("/storage/emulated/0/xxx.pdf");
        }catch(Exception e){
            this.core = null;
        }
    }
    public void releaseBitmaps() {
        recycleBitmap(mEntireBm);
        mEntireBm = null;
        recycleBitmap(mPatchBm);
        mPatchBm = null;
    }
    public void setPage(int page, PointF size) {
        pdfSize = correctBugMuPdf(size);
        setParentSize(new Point((int)size.x, (int)size.y));
        if (mEntireBm == null ) {
            try {
                mEntireBm = Bitmap.createBitmap(mParentSize.x, mParentSize.y, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        mSourceScale = Math.min(mParentSize.x / size.x, mParentSize.y / size.y);
        Point newSize = new Point((int) (size.x * mSourceScale), (int) (size.y * mSourceScale));
        mSize = newSize;
        MuPDFCore.Cookie cookie = core.new Cookie();
        try{
            core.drawPage(mEntireBm, page,mSize.x, mSize.y, 0, 0, mSize.x, mSize.y, cookie);
        }catch(Exception e){

        }
        saveBitmapFile(sharpenImageAmeliorate(mEntireBm));
        releaseBitmaps();
    }


    //锐化图片
    private Bitmap sharpenImageAmeliorate(Bitmap bmp)
    {
        // 拉普拉斯矩阵
        int[] laplacian = new int[] { -1, -1, -1, -1, 9, -1, -1, -1, -1 };
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int pixColor = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int idx = 0;
        float alpha = 0.7F;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1, length = height - 1; i < length; i++)
        {
            for (int k = 1, len = width - 1; k < len; k++)
            {
                idx = 0;
                if(pixels[i*width+k] != -1){
                    for (int m = -1; m <= 1; m++)
                    {
                        for (int n = -1; n <= 1; n++)
                        {
                            pixColor = pixels[(i + n) * width + k + m];
                            pixR = Color.red(pixColor);
                            pixG = Color.green(pixColor);
                            pixB = Color.blue(pixColor);

                            newR = newR + (int) (pixR * laplacian[idx] * alpha);
                            newG = newG + (int) (pixG * laplacian[idx] * alpha);
                            newB = newB + (int) (pixB * laplacian[idx] * alpha);
                            idx++;
                        }
                    }

                    newR = Math.min(255, Math.max(0, newR));
                    newG = Math.min(255, Math.max(0, newG));
                    newB = Math.min(255, Math.max(0, newB));

                    pixels[i * width + k] = Color.argb(255, newR, newG, newB);
                    newR = 0;
                    newG = 0;
                    newB = 0;
                }

            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        long end = System.currentTimeMillis();
        return bitmap;
    }

    public void saveBitmapFile(Bitmap bitmap){
        File file=new File("/storage/emulated/0/"+ System.currentTimeMillis()+".jpg");
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Por defecto la medida de pagina que devuelve MuPdf parece ser dos veces superior al correcto
     *
     * @param size
     * @return
     */
    private PointF correctBugMuPdf(PointF size) {
        return new PointF(size.x / 2, size.y / 2);
    }

    public void setParentSize(Point parentSize) {
        this.mParentSize = parentSize;
    }

    public void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            Log.d(TAG, "Recycling bitmap " + bitmap.toString());
            bitmap.recycle();
        }
    }

    public void makepdf(){
        if (core == null && core.countPages() == 0)
        {
            return ;
        }
        for(int i=0; i < core.countPages();i++){
            setPage(i, core.getPageSize(i));
        }
//        core.onDestroy();
        return ;
    }


}
