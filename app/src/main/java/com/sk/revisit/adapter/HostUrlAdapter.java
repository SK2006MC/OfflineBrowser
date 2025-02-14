package com.sk.revisit.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;
import com.sk.revisit.data.Host;
import com.sk.revisit.data.Url;

import java.util.List;

public class HostUrlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "HostUrlAdapter";
    private static final int VIEW_TYPE_HOST = 0;
    private static final int VIEW_TYPE_URL = 1;

    private List<Host> hostList;

    public HostUrlAdapter(List<Host> hostList) {
        this.hostList = hostList;
    }

    public void setHostList(List<Host> hostList) {
        this.hostList = hostList;
        notifyDataSetChanged();
    }

    /**
     * Calculates the host's position in the hostList for a given URL item position in the RecyclerView.
     *
     * @param urlItemPosition The position of the URL item in the RecyclerView.
     * @return The index of the host in the hostList that contains the URL, or -1 if not found.
     */
    private int getHostPositionForUrl(int urlItemPosition) {
        int currentPosition = 0;
        for (int i = 0; i < hostList.size(); i++) {
            Host host = hostList.get(i);
            // Check if the current position is the host item itself
            if (currentPosition == urlItemPosition) {
                return -1; // It's a host, not a URL
            }
            currentPosition++; // Increment for the host item

            if (host.isExpanded()) {
                int urlsSize = host.getUrls().size();
                if (urlItemPosition < currentPosition + urlsSize) {
                    return i; // Found the host index for the URL item
                }
                currentPosition += urlsSize;
            }
        }
        Log.e(TAG, "getHostPositionForUrl: URL position not found in any host. urlItemPosition: " + urlItemPosition);
        return -1; // URL position not found in any host
    }

    /**
     * Calculates the position of a URL within its host's URL list.
     *
     * @param urlItemPosition The position of the URL item in the RecyclerView.
     * @param hostPosition    The position of the host in the hostList.
     * @return The index of the URL within the host's URL list, or -1 if not found.
     */
    private int getUrlPositionInHost(int urlItemPosition, int hostPosition) {
        if (hostPosition == -1) {
            Log.e(TAG, "getUrlPositionInHost: Invalid host position provided: -1");
            return -1;
        }

        int currentPosition = 0;
        for (int i = 0; i < hostList.size(); i++) {
            Host host = hostList.get(i);
            if (i == hostPosition) {
                if (host.isExpanded()) {
                    int urlIndexInHost = urlItemPosition - currentPosition - 1;
                    if (urlIndexInHost >= 0 && urlIndexInHost < host.getUrls().size()) {
                        return urlIndexInHost;
                    } else {
                        Log.e(TAG, "getUrlPositionInHost: URL position out of bounds for host. urlItemPosition: " + urlItemPosition + ", hostPosition: " + hostPosition);
                        return -1;
                    }
                } else {
                    Log.e(TAG, "getUrlPositionInHost: Host is not expanded. hostPosition: " + hostPosition);
                    return -1;
                }
            }
            currentPosition++; // For the host item
            if (host.isExpanded()) {
                currentPosition += host.getUrls().size();
            }
        }
        Log.e(TAG, "getUrlPositionInHost: Host not found for position: " + hostPosition);
        return -1; // Host not found
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HOST) {
            View view = inflater.inflate(R.layout.host_item, parent, false);
            return new HostViewHolder(view);
        } else if (viewType == VIEW_TYPE_URL) {
            View view = inflater.inflate(R.layout.url_item, parent, false);
            return new UrlViewHolder(view);
        } else {
            throw new IllegalArgumentException("Invalid view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = getItem(position);
        if (holder instanceof HostViewHolder && item instanceof Host) {
            Host host = (Host) item;
            ((HostViewHolder) holder).bind(host);
        } else if (holder instanceof UrlViewHolder && item instanceof Url) {
            Url url = (Url) item;
            ((UrlViewHolder) holder).bind(url);
        } else {
            Log.e(TAG, "onBindViewHolder: Invalid item type or holder type at position: " + position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof Host) {
            return VIEW_TYPE_HOST;
        } else if (item instanceof Url) {
            return VIEW_TYPE_URL;
        } else {
            Log.e(TAG, "getItemViewType: Invalid item type at position: " + position);
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (Host host : hostList) {
            count++;
            if (host.isExpanded()) {
                count += host.getUrls().size();
            }
        }
        return count;
    }

    /**
     * Retrieves the item (Host or Url) at the specified position in the RecyclerView.
     *
     * @param position The position of the item in the RecyclerView.
     * @return The Host or Url object at the specified position, or null if the position is invalid.
     */
    private Object getItem(int position) {
        int currentPosition = 0;
        for (Host host : hostList) {
            if (currentPosition == position) {
                return host;
            }
            currentPosition++;
            if (host.isExpanded()) {
                int urlsSize = host.getUrls().size();
                if (position < currentPosition + urlsSize) {
                    return host.getUrls().get(position - currentPosition);
                }
                currentPosition += urlsSize;
            }
        }
        Log.e(TAG, "getItem: Invalid position: " + position);
        return null;
    }

    // ViewHolder for Host items
    public static class HostViewHolder extends RecyclerView.ViewHolder {
        private final TextView hostNameTextView;

        public HostViewHolder(View itemView) {
            super(itemView);
            hostNameTextView = itemView.findViewById(R.id.hostNameTextView); // Replace with your actual ID
        }

        public void bind(Host host) {
            hostNameTextView.setText(host.getName()); // Replace with your actual method to get the host name
        }
    }

    // ViewHolder for Url items
    public static class UrlViewHolder extends RecyclerView.ViewHolder {
        private final TextView urlTextView;

        public UrlViewHolder(View itemView) {
            super(itemView);
            urlTextView = itemView.findViewById(R.id.urlTextView); // Replace with your actual ID
        }

        public void bind(Url url) {
            urlTextView.setText(url.getUrl()); // Replace with your actual method to get the URL
        }
    }
}