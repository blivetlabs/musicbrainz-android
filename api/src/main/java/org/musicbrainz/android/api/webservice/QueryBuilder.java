/*
 * Copyright (C) 2011 Jamie McDonald
 * 
 * This file is part of MusicBrainz for Android.
 * 
 * MusicBrainz for Android is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * MusicBrainz for Android is distributed in the hope that it 
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MusicBrainz for Android. If not, see 
 * <http://www.gnu.org/licenses/>.
 */

package org.musicbrainz.android.api.webservice;

import org.musicbrainz.android.api.webservice.MBEntity;

public class QueryBuilder {

    private static final String WEB_SERVICE = "http://musicbrainz.org/ws/2/";

    private static final String LOOKUP_ARTIST = "artist/";
    private static final String LOOKUP_ARTIST_PARAMS = "?inc=url-rels+tags+ratings";
    private static final String LOOKUP_RELEASE = "release/";
    private static final String LOOKUP_RELEASE_PARAMS = "?inc=release-groups+artists+recordings+labels+tags+ratings";

    private static final String BROWSE_ARTIST_RGS = "release-group?artist=";
    private static final String BROWSE_ARTIST_RGS_PARAMS = "&limit=100";
    private static final String BROWSE_RG_RELEASES = "release?release-group=";
    private static final String BROWSE_RG_RELEASES_PARAMS = "&inc=artist-credits+labels+mediums";

    private static final String SEARCH_ARTIST = "artist?query=";
    private static final String SEARCH_RG = "release-group?query=";
    private static final String SEARCH_RELEASE = "release?query=";
    private static final String SEARCH_BARCODE = "release/?query=barcode:";
    private static final String SEARCH_BARCODE_PARAMS = "&limit=1";

    private static final String TAG_PARAMS = "?inc=tags";
    private static final String RATING_PARAMS = "?inc=ratings";

    // MBID for Various Artists always exists.
    private static final String AUTH_TEST = "artist/89ad4ac3-39f7-470e-963a-56509c546377?inc=user-tags";
    private static final String CLIENT = "?client=musicbrainz.android-";

    private static final String USER_PARAMS = "?inc=user-tags+user-ratings";
    private static final String TAG = "tag";
    private static final String RATING = "rating";
    private static final String BARCODE = "release/";

    public static String barcodeSearch(String barcode) {
        return new String(WEB_SERVICE + SEARCH_BARCODE + barcode + SEARCH_BARCODE_PARAMS);
    }

    public static String releaseLookup(String mbid) {
        return new String(WEB_SERVICE + LOOKUP_RELEASE + mbid + LOOKUP_RELEASE_PARAMS);
    }

    public static String releaseGroupReleaseBrowse(String mbid) {
        return new String(WEB_SERVICE + BROWSE_RG_RELEASES + mbid + BROWSE_RG_RELEASES_PARAMS);
    }

    public static String artistLookup(String mbid) {
        return new String(WEB_SERVICE + LOOKUP_ARTIST + mbid + LOOKUP_ARTIST_PARAMS);
    }

    public static String artistReleaseGroupBrowse(String mbid) {
        return new String(WEB_SERVICE + BROWSE_ARTIST_RGS + mbid + BROWSE_ARTIST_RGS_PARAMS);
    }

    public static String artistSearch(String searchTerm) {
        return new String(WEB_SERVICE + SEARCH_ARTIST + WebServiceUtils.sanitise(searchTerm));
    }

    public static String releaseGroupSearch(String searchTerm) {
        return new String(WEB_SERVICE + SEARCH_RG + WebServiceUtils.sanitise(searchTerm));
    }

    public static String releaseSearch(String searchTerm) {
        return new String(WEB_SERVICE + SEARCH_RELEASE + WebServiceUtils.sanitise(searchTerm));
    }

    public static String tagLookup(MBEntity type, String mbid) {
        return new String(WEB_SERVICE + WebServiceUtils.entityString(type) + "/" + mbid + TAG_PARAMS);
    }

    public static String ratingLookup(MBEntity type, String mbid) {
        return new String(WEB_SERVICE + WebServiceUtils.entityString(type) + "/" + mbid + RATING_PARAMS);
    }

    public static String authenticationCheck() {
        return new String(WEB_SERVICE + AUTH_TEST);
    }

    public static String userData(MBEntity entity, String mbid) {
        return new String(WEB_SERVICE + WebServiceUtils.entityString(entity) + "/" + mbid + USER_PARAMS);
    }

    public static String tagSubmission(String clientId) {
        return new String(WEB_SERVICE + TAG + CLIENT + clientId);
    }

    public static String ratingSubmission(String clientId) {
        return new String(WEB_SERVICE + RATING + CLIENT + clientId);
    }

    public static String barcodeSubmission(String clientId) {
        return new String(WEB_SERVICE + BARCODE + CLIENT + clientId);
    }

}