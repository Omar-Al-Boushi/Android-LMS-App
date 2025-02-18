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

/**
 * محول لعرض قائمة الملفات في RecyclerView
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {
    private Context context;
    private List<FileData> fileList;
    private int courseColor;
    private FileDownloadListener downloadListener;

    public FilesAdapter(Context context, List<FileData> fileList, int courseColor, FileDownloadListener downloadListener) {
        this.context = context;
        this.fileList = fileList;
        this.courseColor = courseColor;
        this.downloadListener = downloadListener;
    }

    public List<FileData> getFileList() { return fileList; }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardsFilesBinding binding = ItemCardsFilesBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new FileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileData fileData = fileList.get(position);
        holder.bind(fileData);
    }

    @Override
    public int getItemCount() { return fileList.size(); }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        private final ItemCardsFilesBinding binding;

        public FileViewHolder(ItemCardsFilesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FileData fileData) {
            binding.textView.setText(fileData.getFileName());
            binding.startIcon.setImageResource(fileData.getFileTypeIcon());
            binding.endIcon.setImageResource(fileData.getDownloadIcon());

            int backgroundColor = ContextCompat.getColor(context, R.color.Custom_BackgroundColor);
            int textColorBlack = ContextCompat.getColor(context, R.color.Custom_Black);
            int textColorWhite = ContextCompat.getColor(context, android.R.color.white);

            if (fileData.isDownloaded()) {
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

            binding.getRoot().setOnClickListener(v -> {
                if (downloadListener != null) {
                    downloadListener.onFileClicked(fileData);
                }
            });
        }
    }

    public interface FileDownloadListener {
        void onFileClicked(FileData fileData);
    }
}