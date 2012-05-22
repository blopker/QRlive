package org.ninja.qrlive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class QRGenerator {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;
	private static final int dimension = 500;
	private static final String TAG = null;

	Bitmap encodeAsBitmap(String contentsToEncode) throws WriterException {
		if (contentsToEncode == null) {
			return null;
		}
		Map<EncodeHintType, Object> hints = null;
		String encoding = guessAppropriateEncoding(contentsToEncode);
		if (encoding != null) {
			hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			hints.put(EncodeHintType.CHARACTER_SET, encoding);
		}
		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = writer.encode(contentsToEncode,
				BarcodeFormat.QR_CODE, dimension, dimension, hints);
		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	String saveToDisk(Bitmap bitmap){
		File bsRoot = new File(Environment.getExternalStorageDirectory(), "BarcodeScanner");
	    File barcodesRoot = new File(bsRoot, "Barcodes");
	    if (!barcodesRoot.exists() && !barcodesRoot.mkdirs()) {
	      Log.w(TAG, "Couldn't make dir " + barcodesRoot);
	      return null;
	    }
	    File barcodeFile = new File(barcodesRoot, System.currentTimeMillis() + ".png");
	    barcodeFile.delete();
	    FileOutputStream fos = null;
	    try {
	      fos = new FileOutputStream(barcodeFile);
	      bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
	    } catch (FileNotFoundException fnfe) {
	      Log.w(TAG, "Couldn't access file " + barcodeFile + " due to " + fnfe);
	      return null;
	    } finally {
	      if (fos != null) {
	        try {
	          fos.close();
	        } catch (IOException ioe) {
	          // do nothing
	        }
	      }
	    }
	    return barcodeFile.getAbsolutePath();
	}

	private static String guessAppropriateEncoding(CharSequence contents) {
		// Very crude at the moment
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
	}
}
