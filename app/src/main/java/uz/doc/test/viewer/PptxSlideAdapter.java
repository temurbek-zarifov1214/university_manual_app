package uz.doc.test.viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import uz.doc.test.R;

public class PptxSlideAdapter extends RecyclerView.Adapter<PptxSlideAdapter.SlideViewHolder> {

    public static class SlideItem {
        public final int slideNumber;
        public final String text;
        public final byte[] firstImageBytes;

        public SlideItem(int slideNumber, String text, byte[] firstImageBytes) {
            this.slideNumber = slideNumber;
            this.text = text == null ? "" : text;
            this.firstImageBytes = firstImageBytes;
        }
    }

    private final Context context;
    private List<SlideItem> slides = new ArrayList<>();

    public PptxSlideAdapter(Context context) {
        this.context = context;
    }

    public void setSlides(List<SlideItem> slides) {
        this.slides = slides == null ? new ArrayList<>() : slides;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pptx_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.bind(slides.get(position));
    }

    @Override
    public int getItemCount() {
        return slides.size();
    }

    class SlideViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSlideTitle;
        private final TextView tvSlideText;
        private final ImageView ivSlideImage;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSlideTitle = itemView.findViewById(R.id.tv_slide_title);
            tvSlideText = itemView.findViewById(R.id.tv_slide_text);
            ivSlideImage = itemView.findViewById(R.id.iv_slide_image);
        }

        void bind(SlideItem item) {
            tvSlideTitle.setText("Slide " + item.slideNumber);

            if (item.text.trim().isEmpty()) {
                tvSlideText.setVisibility(View.GONE);
            } else {
                tvSlideText.setVisibility(View.VISIBLE);
                tvSlideText.setText(item.text);
            }

            if (item.firstImageBytes != null && item.firstImageBytes.length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(item.firstImageBytes, 0, item.firstImageBytes.length);
                if (bmp != null) {
                    ivSlideImage.setVisibility(View.VISIBLE);
                    ivSlideImage.setImageBitmap(bmp);
                } else {
                    ivSlideImage.setVisibility(View.GONE);
                    ivSlideImage.setImageDrawable(null);
                }
            } else {
                ivSlideImage.setVisibility(View.GONE);
                ivSlideImage.setImageDrawable(null);
            }
        }
    }
}

