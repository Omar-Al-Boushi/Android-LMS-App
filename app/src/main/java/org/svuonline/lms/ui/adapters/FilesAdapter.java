package org.svuonline.lms.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.svuonline.lms.R;
import org.svuonline.lms.databinding.ItemCardsFilesBinding;
import org.svuonline.lms.ui.data.FileData;

import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private Context context;
    private List<FileData> fileList;
    private int courseColor; // قيمة اللون الفعلية
    private FileDownloadListener downloadListener;

    public FilesAdapter(Context context, List<FileData> fileList, int courseColor, FileDownloadListener downloadListener) {
        this.context = context;
        this.fileList = fileList;
        this.courseColor = courseColor;
        this.downloadListener = downloadListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardsFilesBinding binding = ItemCardsFilesBinding.inflate(
                LayoutInflater.from(context), parent, false
        );
        return new FileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileData fileData = fileList.get(position);
        holder.bind(fileData);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        private final ItemCardsFilesBinding binding;

        public FileViewHolder(ItemCardsFilesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FileData fileData) {
            binding.textView.setText(fileData.getFileName());

            // تحديد أيقونة الملف بناءً على الامتداد وحالة التحميل
            int fileIcon = getFileIconByExtension(fileData.getFileName(), fileData.isDownloaded());
            binding.startIcon.setImageResource(fileIcon);

            // تعيين أيقونة التحميل أو التحميل المكتمل
            binding.endIcon.setImageResource(fileData.getDownloadIcon());

            // الحصول على الألوان من الموارد
            int backgroundColor = ContextCompat.getColor(context, R.color.Custom_BackgroundColor);
            int textColorBlack = ContextCompat.getColor(context, R.color.Custom_Black);
            int textColorWhite = ContextCompat.getColor(context, android.R.color.white);

            if (fileData.isDownloaded()) {
                // حالة التحميل
                binding.cardParent.setCardBackgroundColor(courseColor);
                binding.cardParent.setStrokeWidth(0);
                binding.textView.setTextColor(textColorWhite);
                binding.endIcon.setColorFilter(textColorWhite);
            } else {
                binding.cardParent.setCardBackgroundColor(backgroundColor);
                binding.cardParent.setStrokeColor(courseColor);
                binding.cardParent.setStrokeWidth(4);
                binding.textView.setTextColor(textColorBlack);
                binding.endIcon.setColorFilter(textColorBlack);
            }

            // مستمع للنقر على العنصر
            binding.getRoot().setOnClickListener(v -> {
                if (fileData.isDownloaded()) {
                    // الملف محمَّل، لا تفعل شيئًا
                } else {
                    // الملف غير محمَّل، أبلغ الـ Activity ببدء التحميل
                    if (downloadListener != null) {
                        downloadListener.onDownloadRequested(fileData);
                    }
                }
            });
        }

        // دالة للحصول على الأيقونة المناسبة بناءً على الامتداد وحالة التحميل
        private int getFileIconByExtension(String fileName, boolean isDownloaded) {
            String extension = getFileExtension(fileName);
            switch (extension) {
                case "pdf":
                    return isDownloaded ? R.drawable.pdfwhite : R.drawable.pdf;
                case "lrec":
                    return isDownloaded ? R.drawable.lrecwhite : R.drawable.lrec;
                default:
                    return isDownloaded ? R.drawable.ic_launcher_foreground : R.drawable.ic_launcher_background;
            }
        }

        // دالة للحصول على امتداد الملف
        private String getFileExtension(String fileName) {
            int dotIndex = fileName.lastIndexOf('.');
            if(dotIndex != -1 && dotIndex < fileName.length() - 1) {
                return fileName.substring(dotIndex + 1).toLowerCase();
            } else {
                return ""; // لا يوجد امتداد
            }
        }
    }

    // واجهة للتواصل مع الـ Activity
    public interface FileDownloadListener {
        void onDownloadRequested(FileData fileData);
    }
}
