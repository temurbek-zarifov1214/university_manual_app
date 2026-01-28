package uz.doc.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import uz.doc.test.R;
import uz.doc.test.model.Document;
import uz.doc.test.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private Context context;
    private List<Document> documents;
    private OnDocumentClickListener listener;
    private SharedPrefsHelper prefsHelper;

    public interface OnDocumentClickListener {
        void onDocumentClick(Document document);
        void onFavoriteClick(Document document, boolean isFavorite);
    }

    public DocumentAdapter(Context context, OnDocumentClickListener listener) {
        this.context = context;
        this.documents = new ArrayList<>();
        this.listener = listener;
        this.prefsHelper = SharedPrefsHelper.getInstance(context);
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document document = documents.get(position);
        holder.bind(document);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivFileIcon;
        private TextView tvFileTitle;
        private TextView tvFileType;
        private ImageView ivFavorite;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);

            ivFileIcon = itemView.findViewById(R.id.iv_file_icon);
            tvFileTitle = itemView.findViewById(R.id.tv_file_title);
            tvFileType = itemView.findViewById(R.id.tv_file_type);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }

        public void bind(Document document) {
            // Set file title
            tvFileTitle.setText(document.getTitle());

            // Set file type icon and text
            if (document.getType() == Document.DocumentType.PDF) {
                ivFileIcon.setImageResource(R.drawable.ic_pdf);
                tvFileType.setText(context.getString(R.string.pdf_document));
            } else {
                ivFileIcon.setImageResource(R.drawable.ic_pptx);
                tvFileType.setText(context.getString(R.string.pptx_presentation));
            }

            // Check if favorite
            boolean isFavorite = prefsHelper.isFavorite(document.getId());
            document.setFavorite(isFavorite);
            updateFavoriteIcon(isFavorite);

            // Click on document
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDocumentClick(document);
                }
            });

            // Click on favorite
            ivFavorite.setOnClickListener(v -> {
                boolean newFavoriteState = !document.isFavorite();
                document.setFavorite(newFavoriteState);
                updateFavoriteIcon(newFavoriteState);

                if (listener != null) {
                    listener.onFavoriteClick(document, newFavoriteState);
                }
            });
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                ivFavorite.setImageResource(R.drawable.ic_favorite_border);
            }
        }
    }
}