package com.demo.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidx.LogUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 * description: 
 */
public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_NORMAL = 0;
    public static final int ITEM_TITLE = 1;

    private final ArrayList<GalleryItem> data;
    private Context context;
    private final HashSet<Integer> mSelected;
    private final List<Integer> titleIndex;
    private boolean selectMode = false;
    private ItemClickListener listener;


    public GalleryAdapter(ArrayList<GalleryItem> data) {
        this.data = data;
        mSelected = new HashSet<>();
        titleIndex = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isTitle()) {
                titleIndex.add(i);
            }
        }
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.listener = itemClickListener;
    }

    public boolean isSelectMode() {
        return selectMode;
    }

    public void setSelectMode(boolean select) {
        this.selectMode = select;
    }

    public void toggle() {
        if (selectMode) {
            selectMode = false;
            exitSelect();
        } else {
            selectMode = true;
            enterSelect();
        }
        notifyDataSetChanged();
    }

    public void enterSelect() {
        mSelected.clear();
        updateSelectCount();
        if (listener != null) {
            listener.onSelectModeChange(true);
        }
        notifyDataSetChanged();
    }

    public void exitSelect() {
        mSelected.clear();
        updateSelectCount();
        if (listener != null) {
            listener.onSelectModeChange(false);
        }
    }

    public void unselectAll() {
        mSelected.clear();
        notifyDataSetChanged();
        updateSelectCount();
    }

    public void selectAll() {
        for (int i = 0; i < getItemCount(); i++){
            mSelected.add(i);
        }
        notifyDataSetChanged();
        updateSelectCount();
    }

    public void toggleSelection(int pos) {
        if (mSelected.contains(pos)){
            mSelected.remove(pos);
        } else {
            mSelected.add(pos);
        }
        notifyItemChanged(pos);
        updateSelectCount();
    }

    public void select(int position, boolean selected) {
        if (selected){
            mSelected.add(position);
        } else {
            mSelected.remove(position);
        }
        notifyItemChanged(position);
        updateSelectCount();
    }

    public void selectRange(int start, int end, boolean selected) {
        LogUtils.d("selectRange: start= " + start + "end= " + end + " selected= " + selected);
        for (int i = start; i <= end; i++) {
            if (selected)
                mSelected.add(i);
            else
                mSelected.remove(i);
        }
        notifyItemRangeChanged(start, end - start + 1);
        updateSelectCount();
    }

    public int getSelectedCount() {
        return mSelected.size();
    }

    public int getSelectedPhotosCount() {
        int selectedCount = getSelectedCount();
        for (Integer index : titleIndex) {
            if (mSelected.contains(index)) {
                selectedCount--;
            }
        }
        return selectedCount;
    }

    public HashSet<Integer> getSelection() {
        return mSelected;
    }


    /**
     * 判断是否所有的照片都被选中（排除标题，只计算照片）
     * @return
     */
    public boolean isAllPhotosSelected() {
        return getSelectedPhotosCount() == (getItemCount() - titleIndex.size());
    }

    private void updateSelectCount() {
        if (listener != null) {
            listener.onPhotoSelectChange(getSelectedPhotosCount());
        }
    }

    @Override
    public int getItemViewType(int position) {
        GalleryItem galleryItem = data.get(position);
        return galleryItem.isTitle() ? ITEM_TITLE : ITEM_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        RecyclerView.ViewHolder viewHolder;
        if (ITEM_TITLE == viewType) {
            View inflate = LayoutInflater.from(context).inflate(R.layout.adapter_type_title, parent, false);
            viewHolder = new TitleViewHolder(inflate);
        } else {
            View inflate = LayoutInflater.from(context).inflate(R.layout.adapter_type_gallery, parent, false);
            viewHolder = new NormalViewHolder(inflate);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NormalViewHolder) {
            bindNormalViewHolder((NormalViewHolder) holder, position);
        } else if (holder instanceof TitleViewHolder) {
            bindTitleViewHolder((TitleViewHolder) holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    private void bindNormalViewHolder(@NonNull NormalViewHolder holder, int position) {
        GalleryItem galleryItem = data.get(position);
        Glide.with(context).load(galleryItem.getUri()).into(holder.ivImage);
        if (selectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(mSelected.contains(position));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

    }

    private void bindTitleViewHolder(@NonNull TitleViewHolder holder, int position) {
        GalleryItem galleryItem = data.get(position);
        holder.tvTitle.setText(galleryItem.getTitle());
    }

    public class NormalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private ImageView ivImage;
        private CheckBox checkBox;

        public NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            checkBox = itemView.findViewById(R.id.cb_check);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSelection(getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                listener.onItemLongClick(v, getAdapterPosition());
            }
            return false;
        }
    }

    public class TitleViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onSelectModeChange(boolean select);

        void onPhotoSelectChange(int photoCount);
    }
}
