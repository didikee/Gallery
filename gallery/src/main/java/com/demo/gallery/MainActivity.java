package com.demo.gallery;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidx.picker.MediaItem;
import com.androidx.picker.MediaLoader;
import com.google.android.material.button.MaterialButton;
import dragselectrecyclerview.DragSelectTouchListener;
import dragselectrecyclerview.DragSelectionProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private MaterialButton btSelect;
    private MaterialButton btMode;
    private TextView tvDesc;
    private LinearLayout actionLayout;
    private MaterialButton btShare;
    private MaterialButton btDelete;
    private MaterialButton btMore;

    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
    private DragSelectTouchListener dragSelectTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btSelect = findViewById(R.id.bt_select);
        btMode = findViewById(R.id.bt_mode);
        tvDesc = findViewById(R.id.tv_desc);
        actionLayout = findViewById(R.id.action_layout);
        recyclerView = findViewById(R.id.recyclerView);
        btShare = findViewById(R.id.bt_share);
        btDelete = findViewById(R.id.bt_delete);
        btMore = findViewById(R.id.bt_more);

        btMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryAdapter.toggle();
                updateModeActionUi();
            }
        });
        btSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!galleryAdapter.isSelectMode()) {
                    return;
                }
                if (galleryAdapter.isAllPhotosSelected()) {
                    galleryAdapter.unselectAll();
                } else {
                    galleryAdapter.selectAll();
                }
            }
        });

        btShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Share clicked", Toast.LENGTH_SHORT).show();
            }
        });
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Delete clicked", Toast.LENGTH_SHORT).show();
            }
        });
        btMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "More clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ArrayList<MediaItem> mediaItems = new MediaLoader.Builder(getContentResolver())
                .ofImage()
                .get();
        for (int i = 0; i < Math.min(10, mediaItems.size()); i++) {
            MediaItem mediaItem = mediaItems.get(i);
            Log.d("==", "date_modified: " + mediaItem.getDateModified());
        }

        ArrayList<GalleryItem> todayItems = new ArrayList<>();
        ArrayList<GalleryItem> last7daysItems = new ArrayList<>();
        ArrayList<GalleryItem> earlierItems = new ArrayList<>();
        long currentTimeMillis = System.currentTimeMillis();
        for (int i = 0; i < mediaItems.size(); i++) {
            MediaItem mediaItem = mediaItems.get(i);
            TitleType type = GalleryUtils.offsetDays(currentTimeMillis, mediaItem.getDateModified() * 1000);
            if (type == TitleType.LAST_7_DAYS) {
                last7daysItems.add(new GalleryItem(mediaItem));
            } else if (type == TitleType.TODAY) {
                todayItems.add(new GalleryItem(mediaItem));
            } else if (type == TitleType.EARLIER) {
                earlierItems.add(new GalleryItem(mediaItem));
            }
        }
        if (!todayItems.isEmpty()) {
            todayItems.add(0, new GalleryItem(TitleType.TODAY));
        }
        if (!last7daysItems.isEmpty()) {
            todayItems.add(new GalleryItem(TitleType.LAST_7_DAYS));
            todayItems.addAll(last7daysItems);
        }
        if (!earlierItems.isEmpty()) {
            todayItems.add(new GalleryItem(TitleType.EARLIER));
            for (int i = 0; i < Math.min(earlierItems.size(), 200); i++) {
                todayItems.add(earlierItems.get(i));
            }
//            todayItems.addAll(earlierItems);
        }
        setupRecyclerList(todayItems);


        // reset button state
        updateSelectActionUi();
        updateModeActionUi();
    }

    @Override
    public void onBackPressed() {
        if (galleryAdapter != null && galleryAdapter.isSelectMode()) {
            galleryAdapter.exitSelect();
        } else {
            super.onBackPressed();
        }
    }

    private void setupRecyclerList(ArrayList<GalleryItem> data) {
        final int spanCount = 3;
        galleryAdapter = new GalleryAdapter(data);
        galleryAdapter.setOnItemClickListener(new GalleryAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (galleryAdapter.isSelectMode()) {
                    dragSelectTouchListener.startDragSelection(position);
                }
            }

            @Override
            public void onSelectModeChange(boolean select) {
                if (select) {
                    actionLayout.setVisibility(View.VISIBLE);
                    btSelect.setVisibility(View.VISIBLE);
                    updateModeActionUi();
                    updateSelectActionUi();
                } else {
                    actionLayout.setVisibility(View.GONE);
                    btSelect.setVisibility(View.INVISIBLE);
                    updateModeActionUi();
                }
            }

            @Override
            public void onPhotoSelectChange(int photoCount) {
                String desc = "";
                if (photoCount > 0) {
                    desc = String.format(Locale.getDefault(), "%d selected", photoCount);
                }
                tvDesc.setText(desc);
                updateSelectActionUi();
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int itemViewType = galleryAdapter.getItemViewType(position);
                return itemViewType == GalleryAdapter.ITEM_TITLE ? 3 : 1;
            }
        });
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(gridLayoutManager);

        // drag and select
        dragSelectTouchListener = new DragSelectTouchListener();
        dragSelectTouchListener.withSelectListener(new DragSelectTouchListener.OnDragSelectListener() {
            @Override
            public void onSelectChange(int start, int end, boolean isSelected) {
                galleryAdapter.selectRange(start, end, isSelected);
            }
        });
        DragSelectionProcessor selectionProcessor = new DragSelectionProcessor(new DragSelectionProcessor.ISelectionHandler() {
            @Override
            public HashSet<Integer> getSelection() {
                return galleryAdapter.getSelection();
            }

            @Override
            public boolean isSelected(int index) {
                return galleryAdapter.getSelection().contains(index);
            }

            @Override
            public void updateSelection(int start, int end, boolean isSelected, boolean calledFromOnStart) {
                galleryAdapter.selectRange(start, end, isSelected);
            }
        });
        dragSelectTouchListener.withDebug(true);
        dragSelectTouchListener.withSelectListener(selectionProcessor);
        recyclerView.addOnItemTouchListener(dragSelectTouchListener);

        recyclerView.setAdapter(galleryAdapter);
    }

    private void updateSelectActionUi() {
        if (galleryAdapter.isSelectMode()) {
            if (galleryAdapter.isAllPhotosSelected()) {
                btSelect.setText("Deselect All");
            } else {
                btSelect.setText("Select All");
            }
        } else {
            btSelect.setVisibility(View.INVISIBLE);
        }
    }

    private void updateModeActionUi() {
        boolean selectMode = galleryAdapter.isSelectMode();
        if (selectMode) {
            btMode.setText("Cancel");
        } else {
            btMode.setText("Select");
        }
    }
}