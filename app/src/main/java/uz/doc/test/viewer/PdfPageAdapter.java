package uz.doc.test.viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import uz.doc.test.R;

class PdfPageAdapter extends RecyclerView.Adapter<PdfPageAdapter.PageViewHolder> {

    private final PdfRenderer pdfRenderer;
    private final LayoutInflater inflater;
    private final LruCache<Integer, Bitmap> bitmapCache;
    private final int targetWidth;

    PdfPageAdapter(Context context, PdfRenderer pdfRenderer) {
        this.pdfRenderer = pdfRenderer;
        this.inflater = LayoutInflater.from(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        this.targetWidth = metrics.widthPixels;
        int cacheSize = Math.max(4, pdfRenderer.getPageCount());
        this.bitmapCache = new LruCache<>(cacheSize);
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_pdf_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        Bitmap cached = bitmapCache.get(position);
        if (cached != null) {
            holder.pageImage.setImageBitmap(cached);
            return;
        }

        PdfRenderer.Page page = pdfRenderer.openPage(position);
        int width = targetWidth;
        int height = Math.round((float) width / page.getWidth() * page.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();

        bitmapCache.put(position, bitmap);
        holder.pageImage.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return pdfRenderer.getPageCount();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView pageImage;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            pageImage = itemView.findViewById(R.id.pdf_page_image);
        }
    }
}
