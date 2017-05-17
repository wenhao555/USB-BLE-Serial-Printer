package com.xkdx.serial_test;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/25.
 */

public class FileChooserAdapter extends BaseAdapter {
    private ArrayList<FileInfo> mFileLists;
    private LayoutInflater mLayoutInflater = null;
    private static ArrayList<String> PTT_SUFFIX = new ArrayList<String>();
    private static List<String> photoFileType = new ArrayList<String>();

    static {
        PTT_SUFFIX.add(".ppt");
        PTT_SUFFIX.add(".pptx");
    }

    static {
        photoFileType.add(".PNG");
        photoFileType.add(".JPG");
    }

    public FileChooserAdapter(Context context, ArrayList<FileInfo> fileLists) {
        super();
        mFileLists = fileLists;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mFileLists.size();
    }

    @Override
    public Object getItem(int i) {
        return mFileLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = null;
        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            view = mLayoutInflater.inflate(R.layout.activity_file_chooser_grid_view_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }
        FileInfo fileInfo = (FileInfo) getItem(i);
        holder.tvFileName.setText(fileInfo.getFileName());
        if (fileInfo.isDirectory()) { // 文件夹
            holder.imgFileIcon.setImageResource(R.mipmap.ic_folder_new);
            holder.tvFileName.setTextColor(Color.WHITE);
        } else if (fileInfo.isTxtFile()) {
            holder.imgFileIcon.setImageResource(R.mipmap.txt_new1);
            holder.tvFileName.setTextColor(Color.RED);
        } else if (fileInfo.isPhotoFile()) {
            holder.imgFileIcon.setImageResource(R.mipmap.photo_new1);
            holder.tvFileName.setTextColor(Color.RED);
        } else if (fileInfo.isPPTFile()) { // PPT文件
            holder.imgFileIcon.setImageResource(R.mipmap.ic_ppt);
            holder.tvFileName.setTextColor(Color.RED);
        } else if (fileInfo.isBinFile()) {// Bin文件
            holder.imgFileIcon.setImageResource(R.mipmap.ic_bin);
            holder.tvFileName.setTextColor(Color.BLUE);
        } else { // 未知文件
            holder.imgFileIcon.setImageResource(R.mipmap.ic_file_unknown);
            holder.tvFileName.setTextColor(Color.WHITE);
        }

        return view;
    }

    static class ViewHolder {
        ImageView imgFileIcon;
        TextView tvFileName;

        public ViewHolder(View view) {
            imgFileIcon = (ImageView) view.findViewById(R.id.imgFileIcon);
            tvFileName = (TextView) view.findViewById(R.id.tvFileName);
        }
    }

    enum FileType {//文件类型
        FILE, DIRECTORY;
    }

    static class FileInfo {
        private FileType fileType;
        private String fileName;
        private String filePath;

        public FileInfo(String filePath, String fileName, boolean isDirectory) {
            this.filePath = filePath;
            this.fileName = fileName;
            //文件类型
            fileType = isDirectory ? FileType.DIRECTORY : FileType.FILE;

        }

        public boolean isPPTFile() {//PPT
            if (fileName.lastIndexOf(".") < 0) {
                return false;
            }
            String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
            if (!isDirectory() && PTT_SUFFIX.contains(fileSuffix)) {
                return true;
            } else {
                return false;
            }
        }

        public boolean isTxtFile() {//文本文件
            if (fileName.lastIndexOf(".") < 0) // 没有后缀
                return false;
            String fileSuffix = fileName.substring(fileName.lastIndexOf("."))
                    .toUpperCase();
            if (!isDirectory() && ".TXT".contains(fileSuffix))
                return true;
            else
                return false;
        }

        public boolean isPhotoFile() {//照片文件
            if (fileName.lastIndexOf(".") < 0) // 没有后缀
                return false;
            String fileSuffix = fileName.substring(fileName.lastIndexOf("."))
                    .toUpperCase();
            if (!isDirectory() && photoFileType.contains(fileSuffix))
                return true;
            else
                return false;

        }

        public boolean isBinFile() {//Bin文件
            if (fileName.lastIndexOf(".") < 0)
                return false;
            String fileSuffix = fileName.substring(fileName.lastIndexOf("."))
                    .toUpperCase();
            if (!isDirectory() && ".BIN".contains(fileSuffix)) {
                return true;
            } else {
                return false;
            }

        }

        public boolean isDirectory() {//文件夹
            if (fileType == FileType.DIRECTORY) {
                return true;
            } else {
                return false;
            }
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String toString() {
            return "FileInfo [fileType=" + fileType + ", fileName=" + fileName
                    + ", filePath=" + filePath + "]";
        }
    }

}

