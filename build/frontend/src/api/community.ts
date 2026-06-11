import request from '@/utils/request';

export interface ContentPost {
  id: number;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  text: string;
  images: string[];
  mediaAssets: MediaAsset[];
  videoCover?: string;
  hasVideo?: boolean;
  topics: string[];
  style?: string;
  location?: string;
  longitude?: number;
  latitude?: number;
  visibility?: 'public' | 'followers' | 'private';
  likeCount: number;
  commentCount: number;
  collectCount: number;
  shareCount: number;
  liked: boolean;
  collected: boolean;
  createdAt: number;
}

export interface MediaAsset {
  id: number;
  mediaType: 'image' | 'video';
  url: string;
  originalFilename?: string;
  mimeType?: string;
  fileSize?: number;
  sortOrder?: number;
}

export interface ContentComment {
  id: number;
  postId: number;
  authorId: number;
  authorName: string;
  text: string;
  parentCommentId?: number | null;
  replyToUserId?: number | null;
  createdAt: number;
}

export interface FeedQuery {
  scope?: 'recommend' | 'follow';
  topic?: string;
  style?: string;
  page?: number;
  pageSize?: number;
}

export interface FeedResp {
  list: ContentPost[];
  page: number;
  pageSize: number;
  total: number;
}

export interface CommunityTopic {
  id?: number;
  code?: string;
  name: string;
  count: number;
  hot: boolean;
}

export interface FollowUser {
  id: number;
  name: string;
  avatar: string;
  followed: boolean;
  followerCount: number;
  followeeCount: number;
  followedAt?: number;
}

interface TopicResp {
  id?: number;
  topicCode?: string;
  topicName?: string;
  name?: string;
  postCount?: number | null;
  count?: number;
  hot?: boolean;
}

interface PostResp {
  id: number;
  authorUserId?: number;
  authorId?: number;
  authorName?: string;
  authorAvatar?: string;
  postType?: string;
  contentText?: string;
  text?: string;
  danceStyleId?: number | null;
  locationName?: string | null;
  longitude?: number | string | null;
  latitude?: number | string | null;
  visibility?: 'public' | 'followers' | 'private';
  publishedAt?: string;
  createdAt?: string | number;
  topics?: TopicResp[] | string[];
  mediaAssets?: MediaAssetResp[];
  videoCover?: string;
  style?: string;
  location?: string;
  hasVideo?: boolean;
  likeCount?: number;
  commentCount?: number;
  collectCount?: number;
  shareCount?: number;
  liked?: boolean;
  collected?: boolean;
}

interface MediaAssetResp {
  id: number;
  mediaType?: 'image' | 'video';
  type?: 'image' | 'video';
  url?: string;
  originalFilename?: string;
  name?: string;
  mimeType?: string;
  fileSize?: number;
  sortOrder?: number;
}

interface CommentResp {
  id: number;
  contentPostId?: number;
  postId?: number;
  userId?: number;
  authorId?: number;
  authorName?: string;
  parentCommentId?: number | null;
  replyToUserId?: number | null;
  commentText?: string;
  text?: string;
  createdAt: string | number;
}

interface FeedRespRaw {
  list: PostResp[];
  page: number;
  pageSize: number;
  total: number;
}

interface FollowUserResp {
  userId?: number;
  id?: number;
  name?: string;
  avatar?: string;
  following?: boolean;
  followed?: boolean;
  followerCount?: number;
  followeeCount?: number;
  followedAt?: string | number | null;
}

const STYLE_ID: Record<string, number> = {
  Hiphop: 1,
  Jazz: 2,
  Breaking: 3,
  Locking: 4,
  Popping: 5,
  Kpop: 6,
  Waacking: 7
};

const STYLE_NAME: Record<number, string> = Object.fromEntries(
  Object.entries(STYLE_ID).map(([name, id]) => [id, name])
) as Record<number, string>;

const toMs = (value?: string | number | null) => {
  if (typeof value === 'number') return value;
  if (!value) return Date.now();
  const parsed = Date.parse(value);
  return Number.isNaN(parsed) ? Date.now() : parsed;
};

const absolutizeUrl = (url?: string) => {
  if (!url) return '';
  if (/^https?:\/\//i.test(url) || url.startsWith('blob:') || url.startsWith('data:')) return url;
  const base = String(import.meta.env.VITE_API_BASE ?? '').replace(/\/api\/?$/, '');
  return `${base}${url}`;
};

const mapMedia = (items?: MediaAssetResp[] | null): MediaAsset[] =>
  (items ?? []).map((item) => ({
    id: item.id,
    mediaType: item.mediaType ?? item.type ?? 'image',
    url: absolutizeUrl(item.url),
    originalFilename: item.originalFilename ?? item.name,
    mimeType: item.mimeType,
    fileSize: item.fileSize,
    sortOrder: item.sortOrder
  }));

const mapPost = (p: PostResp): ContentPost => {
  const mediaAssets = mapMedia(p.mediaAssets);
  const images = mediaAssets.filter((item) => item.mediaType === 'image').map((item) => item.url);
  const video = mediaAssets.find((item) => item.mediaType === 'video');
  return {
    id: p.id,
    authorId: p.authorUserId ?? p.authorId ?? 0,
    authorName: p.authorName ?? `舞者${String(p.authorUserId ?? p.authorId ?? 0).slice(-4)}`,
    authorAvatar: p.authorAvatar ?? '',
    text: p.contentText ?? p.text ?? '',
    images,
    mediaAssets,
    videoCover: p.videoCover ?? video?.url,
    hasVideo: Boolean(video) || p.postType === 'video' || Boolean(p.hasVideo),
    topics: (p.topics ?? []).map((t) => (typeof t === 'string' ? t : t.topicName ?? t.name ?? '')),
    style: p.style ?? (p.danceStyleId ? STYLE_NAME[p.danceStyleId] : undefined),
    location: p.locationName ?? p.location ?? undefined,
    longitude: p.longitude == null ? undefined : Number(p.longitude),
    latitude: p.latitude == null ? undefined : Number(p.latitude),
    visibility: p.visibility ?? 'public',
    likeCount: p.likeCount ?? 0,
    commentCount: p.commentCount ?? 0,
    collectCount: p.collectCount ?? 0,
    shareCount: p.shareCount ?? 0,
    liked: Boolean(p.liked),
    collected: Boolean(p.collected),
    createdAt: toMs(p.publishedAt ?? p.createdAt)
  };
};

const mapFeed = (data: FeedRespRaw): FeedResp => ({
  list: (data.list ?? []).map(mapPost),
  page: data.page,
  pageSize: data.pageSize,
  total: data.total
});

const mapFollowUser = (u: FollowUserResp): FollowUser => {
  const id = u.userId ?? u.id ?? 0;
  return {
    id,
    name: u.name ?? `舞者${String(id).slice(-4)}`,
    avatar: u.avatar ?? '',
    followed: Boolean(u.following ?? u.followed),
    followerCount: u.followerCount ?? 0,
    followeeCount: u.followeeCount ?? 0,
    followedAt: u.followedAt == null ? undefined : toMs(u.followedAt)
  };
};

const mapReason = (reason: string) => {
  if (reason.includes('广告') || reason.includes('引流')) return 'spam';
  if (reason.includes('低俗') || reason.includes('不适')) return 'adult';
  if (reason.includes('虚假') || reason.includes('欺诈')) return 'fraud';
  return 'other';
};

export const fetchFeed = (q: FeedQuery) =>
  request
    .get<unknown, FeedRespRaw>('/public/community/feed', {
      params: {
        scope: q.scope,
        topic: q.topic,
        danceStyleId: q.style ? STYLE_ID[q.style] : undefined,
        page: q.page,
        pageSize: q.pageSize
      }
    })
    .then(mapFeed);

export const fetchPostDetail = (id: number) =>
  request.get<unknown, PostResp>(`/public/community/posts/${id}`).then(mapPost);

export const uploadPostMedia = (file: File) => {
  const form = new FormData();
  form.append('file', file);
  return request.post<unknown, MediaAssetResp>('/h5/community/media', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  }).then((item) => mapMedia([item])[0]);
};

export const createPost = (body: {
  text: string;
  mediaAssetIds?: number[];
  hasVideo?: boolean;
  topics?: string[];
  style?: string;
  location?: string;
  longitude?: number;
  latitude?: number;
  visibility?: 'public' | 'followers' | 'private';
  idempotencyToken: string;
}) =>
  request
    .post<unknown, PostResp>('/h5/community/posts', {
      postType: body.hasVideo ? 'video' : 'note',
      contentText: body.text,
      danceStyleId: body.style ? STYLE_ID[body.style] : undefined,
      locationName: body.location,
      longitude: body.longitude,
      latitude: body.latitude,
      visibility: body.visibility ?? 'public',
      topicNames: body.topics,
      mediaAssetIds: body.mediaAssetIds
    })
    .then(mapPost);

export const updatePost = (id: number, body: {
  text: string;
  mediaAssetIds?: number[];
  hasVideo?: boolean;
  topics?: string[];
  style?: string;
  location?: string;
  longitude?: number;
  latitude?: number;
  visibility?: 'public' | 'followers' | 'private';
}) =>
  request
    .put<unknown, PostResp>(`/h5/community/posts/${id}`, {
      postType: body.hasVideo ? 'video' : 'note',
      contentText: body.text,
      danceStyleId: body.style ? STYLE_ID[body.style] : undefined,
      locationName: body.location,
      longitude: body.longitude,
      latitude: body.latitude,
      visibility: body.visibility ?? 'public',
      topicNames: body.topics,
      mediaAssetIds: body.mediaAssetIds
    })
    .then(mapPost);

export const deletePost = (id: number) =>
  request.delete<unknown, { deleted: boolean }>(`/h5/community/posts/${id}`);

export const togglePostLike = (id: number) =>
  request.post<unknown, { liked: boolean; likeCount: number }>(`/h5/community/posts/${id}/like`);

export const togglePostCollect = (id: number) =>
  request.post<unknown, { collected: boolean; collectCount: number }>(`/h5/community/posts/${id}/collect`);

export const sharePost = (id: number, channel: 'wechat' | 'moments' | 'link' | 'copy' | 'system' = 'link') =>
  request.post<unknown, { shared: boolean; shareCount: number; shareUrl: string }>(
    `/h5/community/posts/${id}/share`,
    { channel }
  );

export const reportPost = (id: number, reason: string) =>
  request.post<unknown, { reported: boolean }>(`/h5/community/posts/${id}/report`, {
    reasonCode: mapReason(reason),
    reasonDetail: reason
  });

export const reportComment = (id: number, reason: string) =>
  request.post<unknown, { reported: boolean }>(`/h5/community/comments/${id}/report`, {
    reasonCode: mapReason(reason),
    reasonDetail: reason
  });

export const fetchComments = (postId: number) =>
  request.get<unknown, CommentResp[]>(`/public/community/posts/${postId}/comments`).then((items) =>
    (items ?? []).map((c) => {
      const userId = c.userId ?? c.authorId ?? 0;
      return {
        id: c.id,
        postId: c.contentPostId ?? c.postId ?? postId,
        authorId: userId,
        authorName: c.authorName ?? `舞者${String(userId).slice(-4)}`,
        text: c.commentText ?? c.text ?? '',
        parentCommentId: c.parentCommentId ?? null,
        replyToUserId: c.replyToUserId ?? null,
        createdAt: toMs(c.createdAt)
      };
    })
  );

export const createComment = (
  postId: number,
  text: string,
  opts?: { parentCommentId?: number | null; replyToUserId?: number | null }
) =>
  request
    .post<unknown, CommentResp>(`/h5/community/posts/${postId}/comments`, {
      commentText: text,
      parentCommentId: opts?.parentCommentId ?? null,
      replyToUserId: opts?.replyToUserId ?? null
    })
    .then((c) => {
      const userId = c.userId ?? c.authorId ?? 0;
      return {
        id: c.id,
        postId: c.contentPostId ?? c.postId ?? postId,
        authorId: userId,
        authorName: c.authorName ?? `舞者${String(userId).slice(-4)}`,
        text: c.commentText ?? c.text ?? '',
        parentCommentId: c.parentCommentId ?? null,
        replyToUserId: c.replyToUserId ?? null,
        createdAt: toMs(c.createdAt)
      };
    });

export const deleteComment = (id: number) =>
  request.delete<unknown, { deleted: boolean }>(`/h5/community/comments/${id}`);

const mapTopic = (t: TopicResp): CommunityTopic => ({
  id: t.id,
  code: t.topicCode,
  name: t.topicName ?? t.name ?? '',
  count: t.postCount ?? t.count ?? 0,
  hot: Boolean(t.hot)
});

export const fetchTopics = (q?: { scope?: 'hot' | 'new'; keyword?: string; limit?: number }) =>
  request.get<unknown, TopicResp[]>('/public/community/topics', {
    params: {
      scope: q?.scope,
      q: q?.keyword,
      limit: q?.limit
    }
  }).then((items) =>
    (items ?? []).map((t) => ({
      ...mapTopic(t)
    }))
  );

export const fetchTopicDetail = (topic: string) =>
  request.get<unknown, TopicResp>(`/public/community/topics/${encodeURIComponent(topic)}`).then(mapTopic);

export const createTopic = (body: { name: string; description?: string }) =>
  request.post<unknown, TopicResp>('/h5/community/topics', {
    topicName: body.name,
    description: body.description
  }).then(mapTopic);

export const fetchTopicPosts = (topic: string, q?: { sort?: 'hot' | 'new'; page?: number; pageSize?: number }) =>
  request
    .get<unknown, FeedRespRaw>(`/public/community/topics/${encodeURIComponent(topic)}/posts`, {
      params: { sort: q?.sort, page: q?.page ?? 1, pageSize: q?.pageSize ?? 50 }
    })
    .then(mapFeed);

export const toggleFollow = (userId: number) =>
  request.post<unknown, { following: boolean; followerCount: number; followeeCount: number }>(`/h5/community/follow/${userId}`);

export const fetchMyFolloweeIds = () =>
  request.get<unknown, number[]>('/h5/community/follow/me');

export const fetchFollowing = () =>
  request.get<unknown, FollowUserResp[]>('/h5/community/follow/following').then((items) =>
    (items ?? []).map(mapFollowUser)
  );

export const fetchFollowers = () =>
  request.get<unknown, FollowUserResp[]>('/h5/community/follow/followers').then((items) =>
    (items ?? []).map(mapFollowUser)
  );

export const fetchFollowStatus = (userId: number) =>
  request.get<unknown, { userId: number; following: boolean; followerCount: number; followeeCount: number }>(
    `/h5/community/follow/${userId}/status`
  );

export const searchContent = (q: string) =>
  request
    .get<unknown, FeedRespRaw>('/public/community/search', { params: { q } })
    .then(mapFeed);
