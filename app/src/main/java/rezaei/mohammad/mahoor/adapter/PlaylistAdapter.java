package rezaei.mohammad.mahoor.adapter;

import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import rezaei.mohammad.mahoor.adapter.base.AbsMultiSelectAdapter;
import rezaei.mohammad.mahoor.adapter.base.MediaEntryViewHolder;
import rezaei.mohammad.mahoor.dialogs.ClearSmartPlaylistDialog;
import rezaei.mohammad.mahoor.dialogs.DeletePlaylistDialog;
import rezaei.mohammad.mahoor.helper.menu.PlaylistMenuHelper;
import rezaei.mohammad.mahoor.helper.menu.SongsMenuHelper;
import rezaei.mohammad.mahoor.interfaces.CabHolder;
import rezaei.mohammad.mahoor.loader.PlaylistSongLoader;
import rezaei.mohammad.mahoor.model.AbsCustomPlaylist;
import rezaei.mohammad.mahoor.model.Playlist;
import rezaei.mohammad.mahoor.model.Song;
import rezaei.mohammad.mahoor.model.smartplaylist.AbsSmartPlaylist;
import rezaei.mohammad.mahoor.model.smartplaylist.LastAddedPlaylist;
import rezaei.mohammad.mahoor.util.MusicUtil;
import rezaei.mohammad.mahoor.util.NavigationUtil;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import rezaei.mohammad.mahoor.R;

import java.util.ArrayList;
import java.util.List;

import rezaei.mohammad.mahoor.adapter.base.AbsMultiSelectAdapter;
import rezaei.mohammad.mahoor.adapter.base.MediaEntryViewHolder;
import rezaei.mohammad.mahoor.dialogs.ClearSmartPlaylistDialog;
import rezaei.mohammad.mahoor.dialogs.DeletePlaylistDialog;
import rezaei.mohammad.mahoor.interfaces.CabHolder;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistAdapter extends AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist> {

    public static final String TAG = PlaylistAdapter.class.getSimpleName();

    private static final int SMART_PLAYLIST = 0;
    private static final int DEFAULT_PLAYLIST = 1;

    protected final AppCompatActivity activity;
    protected ArrayList<Playlist> dataSet;
    protected int itemLayoutRes;

    public PlaylistAdapter(AppCompatActivity activity, ArrayList<Playlist> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_playlists_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        setHasStableIds(true);
    }

    public ArrayList<Playlist> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(ArrayList<Playlist> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view, viewType);
    }

    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Playlist playlist = dataSet.get(position);

        holder.itemView.setActivated(isChecked(playlist));

        if (holder.title != null) {
            holder.title.setText(playlist.name);
        }

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null && !(dataSet.get(position) instanceof AbsSmartPlaylist)) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        if (holder.image != null) {
            holder.image.setImageResource(getIconRes(playlist));
        }
    }

    private int getIconRes(Playlist playlist) {
        if (playlist instanceof AbsSmartPlaylist) {
            return ((AbsSmartPlaylist) playlist).iconRes;
        }
        return MusicUtil.isFavoritePlaylist(activity, playlist) ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_queue_music_white_24dp;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position) instanceof AbsSmartPlaylist ? SMART_PLAYLIST : DEFAULT_PLAYLIST;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Playlist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Playlist playlist) {
        return playlist.name;
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Playlist> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_playlist:
                for (int i = 0; i < selection.size(); i++) {
                    Playlist playlist = selection.get(i);
                    if (playlist instanceof AbsSmartPlaylist) {
                        AbsSmartPlaylist absSmartPlaylist = (AbsSmartPlaylist) playlist;
                        ClearSmartPlaylistDialog.create(absSmartPlaylist).show(activity.getSupportFragmentManager(), "CLEAR_PLAYLIST_" + absSmartPlaylist.name);
                        selection.remove(playlist);
                        i--;
                    }
                }
                if (selection.size() > 0) {
                    DeletePlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                }
                break;
            default:
                SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
                break;
        }
    }

    @NonNull
    private ArrayList<Song> getSongList(@NonNull List<Playlist> playlists) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            if (playlist instanceof AbsCustomPlaylist) {
                songs.addAll(((AbsCustomPlaylist) playlist).getSongs(activity));
            } else {
                songs.addAll(PlaylistSongLoader.getPlaylistSongList(activity, playlist.id));
            }
        }
        return songs;
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);

            if (itemViewType == SMART_PLAYLIST) {
                if (shortSeparator != null) {
                    shortSeparator.setVisibility(View.GONE);
                }
                itemView.setBackgroundColor(ATHUtil.resolveColor(activity, R.attr.cardBackgroundColor));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(activity.getResources().getDimensionPixelSize(R.dimen.card_elevation));
                }
            }

            if (image != null) {
                int iconPadding = activity.getResources().getDimensionPixelSize(R.dimen.list_item_image_icon_padding);
                image.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
                image.setColorFilter(ATHUtil.resolveColor(activity, R.attr.iconColor), PorterDuff.Mode.SRC_IN);
            }

            if (menu != null) {
                menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Playlist playlist = dataSet.get(getAdapterPosition());
                        final PopupMenu popupMenu = new PopupMenu(activity, view);
                        popupMenu.inflate(getItemViewType() == SMART_PLAYLIST ? R.menu.menu_item_smart_playlist : R.menu.menu_item_playlist);
                        if (playlist instanceof LastAddedPlaylist) {
                            popupMenu.getMenu().findItem(R.id.action_clear_playlist).setVisible(false);
                        }
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {
                                if (item.getItemId() == R.id.action_clear_playlist) {
                                    if (playlist instanceof AbsSmartPlaylist) {
                                        ClearSmartPlaylistDialog.create((AbsSmartPlaylist) playlist).show(activity.getSupportFragmentManager(), "CLEAR_SMART_PLAYLIST_" + playlist.name);
                                        return true;
                                    }
                                }
                                return PlaylistMenuHelper.handleMenuClick(
                                        activity, dataSet.get(getAdapterPosition()), item);
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        }

        @Override
        public void onClick(View view) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Playlist playlist = dataSet.get(getAdapterPosition());
                NavigationUtil.goToPlaylist(activity, playlist);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
