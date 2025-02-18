package org.svuonline.lms.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import org.svuonline.lms.R;

import java.util.List;

public class SelectedFilesAdapter extends RecyclerView.Adapter<SelectedFilesAdapter.ViewHolder> {

    private Context context;
    private List<Uri> fileList;
    private int strokeColor; // اللون الذي سيتم تمريره من النشاط
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // تعديل المُنشئ لاستقبال strokeColor
    public SelectedFilesAdapter(Context context, List<Uri> fileList, int strokeColor) {
        this.context = context;
        this.fileList = fileList;
        this.strokeColor = strokeColor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate للبطاقة المصممة (item_selected_file.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_file, parent, false);
        return new ViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Uri fileUri = fileList.get(position);
        String fileName = getFileName(fileUri);
        holder.textView.setText(fileName);

        // تعيين لون الستروك باستخدام اللون الممرر من النشاط
        holder.cardParent.setStrokeColor(strokeColor);

        // مثال لتعيين الأيقونة بناءً على نوع الملف
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            holder.startIcon.setImageResource(R.drawable.pdf);
        } else {
            // أيقونة افتراضية لملفات غير pdf
            holder.startIcon.setImageResource(R.drawable.lrec);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardParent;
        ShapeableImageView startIcon;
        TextView textView;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            // ربط عناصر البطاقة
            cardParent = itemView.findViewById(R.id.cardParent);
            startIcon = itemView.findViewById(R.id.startIcon);
            textView = itemView.findViewById(R.id.textView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(pos);
                    }
                }
            });
        }
    }

    // طريقة مساعدة للحصول على اسم الملف من Uri مع تسجيل معلومات للتتبع
    private String getFileName(Uri uri) {
        String result = null;
        Log.d("SelectedFilesAdapter", "getFileName called with URI: " + uri);
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null) {
                    Log.d("SelectedFilesAdapter", "Cursor count: " + cursor.getCount());
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        Log.d("SelectedFilesAdapter", "DISPLAY_NAME column index: " + index);
                        if (index != -1) {
                            result = cursor.getString(index);
                            Log.d("SelectedFilesAdapter", "Obtained file name: " + result);
                        } else {
                            Log.d("SelectedFilesAdapter", "DISPLAY_NAME column not found");
                        }
                    } else {
                        Log.d("SelectedFilesAdapter", "Cursor could not move to first");
                    }
                } else {
                    Log.d("SelectedFilesAdapter", "Cursor is null for URI: " + uri);
                }
            } catch (Exception e) {
                Log.e("SelectedFilesAdapter", "Error querying file name", e);
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
            Log.d("SelectedFilesAdapter", "Using getLastPathSegment, result: " + result);
        }
        return result;
    }
}
