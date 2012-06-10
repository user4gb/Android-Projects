package com.avai.wpzoo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;

public class ItemSearch {
	public static class Item {
		public final int id;
		public final String name;
		public final String content;
		
		public Item(int id, String name, String content) {
			this.id = id;
			this.name = name;
			this.content = content;
		}
	}
	
    private static final ItemSearch sInstance = new ItemSearch();

    public static ItemSearch getInstance() {
        return sInstance;
    }

    private final Map<String, List<Item>> mDict = new ConcurrentHashMap<String, List<Item>>();

    private ItemSearch() {
    }

    private boolean mLoaded = false;

    /**
     * Loads the words and definitions if they haven't been loaded already.
     *
     * @param resources Used to load the file containing the words and definitions.
     */
    public synchronized void ensureLoaded(final Context context) {
        if (mLoaded) return;

        new Thread(new Runnable() {
            public void run() {
                try {
                    loadItems(context);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } 
            }
        }).start();
    }
        
	private synchronized void loadItems(Context context) throws IOException {
        if (mLoaded) return;
        System.out.println("Loading words!!!");
        ArrayList<HashMap<String, Object>> items = NavigationHelper.getMenuItems(context, 38);
        for (int i=0;i<items.size();i++) {
        	addItem(((Integer)(items.get(i).get("_id"))).intValue(), (String)items.get(i).get("Name"), (String)items.get(i).get("Content"));
        }
        mLoaded = true;
    }

	@SuppressWarnings("unchecked")
	public List<Item> getMatches(String query) {
        List<ItemSearch.Item> list = mDict.get(query);
        return list == null ? Collections.EMPTY_LIST : list;
    }

    private void addItem(int id, String name, String content) {
        final Item item = new Item(id, name, content);

        final int len = name.length();
        for (int i = 0; i < len; i++) {
            final String prefix = name.substring(0, len - i);
            addMatch(prefix, item);
        }
    }

    private void addMatch(String query, Item item) {
        List<Item> matches = mDict.get(query);
        if (matches == null) {
            matches = new ArrayList<Item>();
            mDict.put(query, matches);
        }
        matches.add(item);
    }
}
