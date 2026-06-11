import { mock } from '../index';

const POST_KEY = 'bitdance_mock_posts';
const COMMENT_KEY = 'bitdance_mock_comments';
const FOLLOW_KEY = 'bitdance_mock_follow';
const TOPIC_KEY = 'bitdance_mock_topics';
const MEDIA_KEY = 'bitdance_mock_media_assets';

interface Post {
  id: number;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  text: string;
  images: string[];
  mediaAssets?: MediaAsset[];
  hasVideo?: boolean;
  topics: string[];
  style?: string;
  location?: string;
  locationName?: string;
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

interface Comment {
  id: number;
  postId: number;
  authorId: number;
  authorName: string;
  text: string;
  parentCommentId?: number | null;
  replyToUserId?: number | null;
  createdAt: number;
}

interface Topic {
  id: number;
  topicCode: string;
  topicName: string;
  postCount: number;
  hot: boolean;
}

interface MediaAsset {
  id: number;
  mediaType: 'image' | 'video';
  url: string;
  originalFilename?: string;
  mimeType?: string;
  fileSize?: number;
  sortOrder?: number;
}

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop'];
const STYLE_BY_ID = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'];
const TOPICS = ['零基础打卡', '街舞日常', '试听感受', '舞室探店', 'Workshop 速记'];
const NICK = ['小喵', '舞月', '阿橘', '云朵', '羊羊', '团团', 'Aki'];

const seed = (): Post[] => {
  const out: Post[] = [];
  for (let i = 1; i <= 24; i += 1) {
    const tArr = TOPICS.slice(i % 4, (i % 4) + 1 + (i % 3));
    out.push({
      id: i,
      authorId: 200 + i,
      authorName: NICK[i % NICK.length],
      authorAvatar: '',
    text: ['今日打卡：', '试听记录：', 'Workshop 复盘：', '搭子招募：'][i % 4] + STYLES[i % STYLES.length] + ' 状态在线',
      images: i % 3 === 0 ? [] : ['p1', 'p2'].slice(0, (i % 2) + 1),
      hasVideo: i % 5 === 0,
      topics: tArr,
      style: STYLES[i % STYLES.length],
      location: ['海淀区舞星 Studio', '朝阳区灵动空间', '浦东新区舞蹈坊'][i % 3],
      likeCount: 5 + i * 3,
      commentCount: i % 5,
      collectCount: i % 7,
      shareCount: i % 4,
      liked: false,
      collected: false,
      createdAt: Date.now() - i * 7200_000
    });
  }
  return out;
};

const loadPosts = (): Post[] => {
  try {
    const raw = localStorage.getItem(POST_KEY);
    return (raw ? (JSON.parse(raw) as Post[]) : seed()).map(normalizePost);
  } catch {
    return seed().map(normalizePost);
  }
};
const savePosts = (items: Post[]) => localStorage.setItem(POST_KEY, JSON.stringify(items));

const loadComments = (): Comment[] => {
  try {
    return JSON.parse(localStorage.getItem(COMMENT_KEY) ?? '[]') as Comment[];
  } catch {
    return [];
  }
};
const saveComments = (items: Comment[]) => localStorage.setItem(COMMENT_KEY, JSON.stringify(items));

const loadFollow = (): number[] => {
  try {
    return JSON.parse(localStorage.getItem(FOLLOW_KEY) ?? '[]') as number[];
  } catch {
    return [];
  }
};
const saveFollow = (ids: number[]) => localStorage.setItem(FOLLOW_KEY, JSON.stringify(ids));

const loadCustomTopics = (): Topic[] => {
  try {
    return JSON.parse(localStorage.getItem(TOPIC_KEY) ?? '[]') as Topic[];
  } catch {
    return [];
  }
};
const saveCustomTopics = (items: Topic[]) => localStorage.setItem(TOPIC_KEY, JSON.stringify(items));

const mediaAssetUrl = (id: number, type: 'image' | 'video') =>
  type === 'video'
    ? 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4'
    : `https://picsum.photos/seed/bitdance-${id}/640/640`;

const loadMediaAssets = (): MediaAsset[] => {
  try {
    return JSON.parse(localStorage.getItem(MEDIA_KEY) ?? '[]') as MediaAsset[];
  } catch {
    return [];
  }
};

const saveMediaAssets = (items: MediaAsset[]) => {
  try {
    localStorage.setItem(MEDIA_KEY, JSON.stringify(items));
  } catch {
    localStorage.setItem(MEDIA_KEY, JSON.stringify(items.filter((item) => item.mediaType === 'image').slice(0, 24)));
  }
};

const rememberMediaAsset = (asset: MediaAsset) => {
  const items = loadMediaAssets().filter((item) => item.id !== asset.id);
  items.unshift(asset);
  saveMediaAssets(items.slice(0, 80));
};

const findMediaAsset = (id: number) => loadMediaAssets().find((item) => item.id === id);

const fileToDataUrl = (file: File) =>
  new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result ?? ''));
    reader.onerror = () => reject(reader.error ?? new Error('read file failed'));
    reader.readAsDataURL(file);
  });

const mediaAssetFromId = (
  id: number,
  index: number,
  type: 'image' | 'video',
  existing?: MediaAsset
): MediaAsset => {
  const saved = existing ?? findMediaAsset(id);
  if (saved) {
    return {
      ...saved,
      mediaType: saved.mediaType ?? type,
      sortOrder: index
    };
  }
  return {
    id,
    mediaType: type,
    url: mediaAssetUrl(id, type),
    originalFilename: `mock-${id}.${type === 'video' ? 'mp4' : 'jpg'}`,
    mimeType: type === 'video' ? 'video/mp4' : 'image/jpeg',
    sortOrder: index
  };
};

const normalizePost = (post: Post): Post => {
  const viewer = currentUser();
  const shouldMigrateLegacyAuthor =
    post.authorName === '我' ||
    (post.authorId === 1 && post.id > 1_000_000_000_000 && viewer.id !== 1);
  const mediaAssets = post.mediaAssets ?? post.images.map((_, index) => {
    const id = post.id * 100 + index + 1;
    return {
      id,
      mediaType: 'image' as const,
      url: mediaAssetUrl(id, 'image'),
      originalFilename: `mock-${id}.jpg`,
      mimeType: 'image/jpeg',
      sortOrder: index
    };
  });
  return {
    ...post,
    authorId: shouldMigrateLegacyAuthor ? viewer.id : post.authorId,
    authorName: shouldMigrateLegacyAuthor ? viewer.name : post.authorName,
    authorAvatar: shouldMigrateLegacyAuthor ? viewer.avatar : post.authorAvatar,
    mediaAssets,
    locationName: post.locationName ?? post.location,
    visibility: post.visibility ?? 'public'
  };
};

const currentUser = () => {
  try {
    const raw = localStorage.getItem('bitdance_profile');
    const profile = raw ? JSON.parse(raw) as { id?: number; nickname?: string; avatar?: string } : null;
    const id = Number(profile?.id);
    return {
      id: Number.isFinite(id) && id > 0 ? id : 1,
      name: profile?.nickname?.trim() || '我',
      avatar: profile?.avatar ?? ''
    };
  } catch {
    return { id: 1, name: '我', avatar: '' };
  }
};

const currentUserId = () => currentUser().id;

const aggregateTopics = (keyword?: string): Topic[] => {
  const counts = new Map<string, number>();
  loadPosts().forEach((p) => p.topics.forEach((t) => counts.set(t, (counts.get(t) ?? 0) + 1)));
  const merged = new Map<string, Topic>();
  Array.from(counts.entries()).forEach(([name, count], index) => {
    merged.set(name, {
      id: index + 1,
      topicCode: `mock-${index + 1}`,
      topicName: name,
      postCount: count,
      hot: count >= 4
    });
  });
  loadCustomTopics().forEach((topic) => {
    const count = counts.get(topic.topicName) ?? topic.postCount ?? 0;
    merged.set(topic.topicName, { ...topic, postCount: count, hot: count >= 4 || topic.hot });
  });
  return Array.from(merged.values())
    .filter((topic) => !keyword || topic.topicName.toLowerCase().includes(keyword.toLowerCase()))
    .sort((a, b) => b.postCount - a.postCount || b.id - a.id);
};

mock('get', /\/community\/feed$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  let items = loadPosts();
  if (p.topic) items = items.filter((it) => it.topics.includes(p.topic as string));
  if (p.danceStyleId) items = items.filter((it) => it.style === STYLE_BY_ID[Number(p.danceStyleId) - 1]);
  if (p.style) items = items.filter((it) => it.style === p.style);
  const followed = loadFollow();
  if (p.scope === 'follow') {
    items = items.filter((it) => followed.includes(it.authorId));
  }
  items = items.slice().sort((a, b) => {
    if (p.scope !== 'follow') {
      const aFollowed = followed.includes(a.authorId) ? 0 : 1;
      const bFollowed = followed.includes(b.authorId) ? 0 : 1;
      if (aFollowed !== bFollowed) return aFollowed - bFollowed;
    }
    return b.createdAt - a.createdAt;
  });
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);
  const start = (page - 1) * pageSize;
  return {
    list: items.slice(start, start + pageSize),
    page,
    pageSize,
    total: items.length
  };
});

mock('get', /\/community\/posts\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  return loadPosts().find((it) => it.id === id) ?? null;
});

mock('post', /\/community\/media$/, async ({ data }) => {
  const file = data instanceof FormData ? data.get('file') as File | null : null;
  const type = file?.type?.startsWith('video/') ? 'video' : 'image';
  const id = Date.now() + Math.floor(Math.random() * 1000);
  const asset: MediaAsset = {
    id,
    mediaType: type,
    url: file ? await fileToDataUrl(file) : mediaAssetUrl(id, type),
    originalFilename: file?.name ?? `mock-${id}.${type === 'video' ? 'mp4' : 'jpg'}`,
    mimeType: file?.type ?? (type === 'video' ? 'video/mp4' : 'image/jpeg'),
    fileSize: file?.size ?? 0,
    sortOrder: 0
  };
  rememberMediaAsset(asset);
  return asset;
});

mock('post', /\/community\/posts$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const topicNames = (body.topicNames as string[] | undefined) ?? (body.topics as string[] | undefined) ?? [];
  const mediaAssetIds = (body.mediaAssetIds as number[] | undefined) ?? [];
  const mediaAssets: MediaAsset[] = mediaAssetIds.map((id, index) => {
    const type = body.postType === 'video' && index === 0 ? 'video' : 'image';
    return mediaAssetFromId(id, index, type);
  });
  const items = loadPosts();
  const author = currentUser();
  const item: Post = {
    id: Date.now(),
    authorId: author.id,
    authorName: author.name,
    authorAvatar: author.avatar,
    text: ((body.contentText as string) ?? (body.text as string)) ?? '',
    images: mediaAssets.filter((item) => item.mediaType === 'image').map((item) => item.url),
    mediaAssets,
    hasVideo: body.postType === 'video' || Boolean(body.hasVideo),
    topics: topicNames,
    style: (body.style as string | undefined) ?? STYLE_BY_ID[Number(body.danceStyleId ?? 0) - 1],
    location: (body.locationName as string | undefined) ?? (body.location as string | undefined),
    locationName: (body.locationName as string | undefined) ?? (body.location as string | undefined),
    longitude: body.longitude == null ? undefined : Number(body.longitude),
    latitude: body.latitude == null ? undefined : Number(body.latitude),
    visibility: (body.visibility as Post['visibility']) ?? 'public',
    likeCount: 0,
    commentCount: 0,
    collectCount: 0,
    shareCount: 0,
    liked: false,
    collected: false,
    createdAt: Date.now()
  };
  items.unshift(item);
  savePosts(items);
  return item;
});

mock('put', /\/community\/posts\/\d+$/, ({ url, data }) => {
  const id = Number(url.split('/').pop());
  const body = data as Record<string, unknown>;
  const topicNames = (body.topicNames as string[] | undefined) ?? (body.topics as string[] | undefined) ?? [];
  const mediaAssetIds = (body.mediaAssetIds as number[] | undefined) ?? [];
  const items = loadPosts();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return null;
  const mediaAssets: MediaAsset[] = mediaAssetIds.map((mediaId, index) => {
    const existing = items[idx].mediaAssets?.find((item) => item.id === mediaId);
    const type = body.postType === 'video' && index === 0 ? 'video' : 'image';
    return mediaAssetFromId(mediaId, index, type, existing);
  });
  items[idx] = normalizePost({
    ...items[idx],
    text: ((body.contentText as string) ?? (body.text as string)) ?? items[idx].text,
    mediaAssets,
    images: mediaAssets.filter((item) => item.mediaType === 'image').map((item) => item.url),
    hasVideo: body.postType === 'video' || Boolean(body.hasVideo),
    topics: topicNames,
    style: (body.style as string | undefined) ?? STYLE_BY_ID[Number(body.danceStyleId ?? 0) - 1] ?? items[idx].style,
    location: (body.locationName as string | undefined) ?? (body.location as string | undefined),
    locationName: (body.locationName as string | undefined) ?? (body.location as string | undefined),
    longitude: body.longitude == null ? undefined : Number(body.longitude),
    latitude: body.latitude == null ? undefined : Number(body.latitude),
    visibility: (body.visibility as Post['visibility']) ?? 'public'
  });
  savePosts(items);
  return items[idx];
});

mock('delete', /\/community\/posts\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  savePosts(loadPosts().filter((it) => it.id !== id));
  saveComments(loadComments().filter((comment) => comment.postId !== id));
  return { deleted: true };
});

mock('post', /\/community\/posts\/\d+\/like$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = loadPosts();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return { liked: false, likeCount: 0 };
  items[idx].liked = !items[idx].liked;
  items[idx].likeCount += items[idx].liked ? 1 : -1;
  savePosts(items);
  return { liked: items[idx].liked, likeCount: items[idx].likeCount };
});

mock('post', /\/community\/posts\/\d+\/collect$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = loadPosts();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return { collected: false, collectCount: 0 };
  items[idx].collected = !items[idx].collected;
  items[idx].collectCount += items[idx].collected ? 1 : -1;
  savePosts(items);
  return { collected: items[idx].collected, collectCount: items[idx].collectCount };
});

mock('post', /\/community\/posts\/\d+\/share$/, ({ url, data }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = loadPosts();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return { shared: false, shareCount: 0, shareUrl: `/community/post/${id}` };
  items[idx].shareCount = (items[idx].shareCount ?? 0) + 1;
  savePosts(items);
  return {
    shared: true,
    shareCount: items[idx].shareCount,
    shareUrl: `/community/post/${id}`,
    channel: (data as Record<string, unknown> | undefined)?.channel ?? 'link'
  };
});

mock('post', /\/community\/posts\/\d+\/report$/, () => ({ reported: true }));

mock('post', /\/community\/comments\/\d+\/report$/, () => ({ reported: true }));

mock('get', /\/community\/posts\/\d+\/comments$/, ({ url }) => {
  const postId = Number(url.split('/').slice(-2)[0]);
  return loadComments()
    .filter((c) => c.postId === postId)
    .sort((a, b) => a.createdAt - b.createdAt);
});

mock('post', /\/community\/posts\/\d+\/comments$/, ({ url, data }) => {
  const postId = Number(url.split('/').slice(-2)[0]);
  const body = (data ?? {}) as Record<string, unknown>;
  const comments = loadComments();
  const author = currentUser();
  const c: Comment = {
    id: Date.now(),
    postId,
    authorId: author.id,
    authorName: author.name,
    text: ((body.commentText as string) ?? (body.text as string)) ?? '',
    parentCommentId: (body.parentCommentId as number | null | undefined) ?? null,
    replyToUserId: (body.replyToUserId as number | null | undefined) ?? null,
    createdAt: Date.now()
  };
  comments.push(c);
  saveComments(comments);
  // 更新 post 评论数
  const posts = loadPosts();
  const idx = posts.findIndex((it) => it.id === postId);
  if (idx >= 0) {
    posts[idx].commentCount = comments.filter((x) => x.postId === postId).length;
    savePosts(posts);
  }
  return c;
});

mock('delete', /\/community\/comments\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const comments = loadComments();
  const target = comments.find((c) => c.id === id);
  if (!target) return { deleted: true };
  const next = comments.filter((c) => c.id !== id && c.parentCommentId !== id);
  saveComments(next);
  const posts = loadPosts();
  const idx = posts.findIndex((it) => it.id === target.postId);
  if (idx >= 0) {
    posts[idx].commentCount = next.filter((x) => x.postId === target.postId).length;
    savePosts(posts);
  }
  return { deleted: true };
});

mock('get', /\/community\/topics$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const limit = Number(p.limit ?? 20);
  const items = aggregateTopics(p.q as string | undefined);
  if (p.scope === 'new') return items.sort((a, b) => b.id - a.id).slice(0, limit);
  return items.slice(0, limit);
});

mock('post', /\/community\/topics$/, ({ data }) => {
  const body = (data ?? {}) as Record<string, unknown>;
  const name = String(body.topicName ?? body.name ?? '').trim().replace(/^#+/, '').trim();
  const existing = aggregateTopics().find((topic) => topic.topicName === name);
  if (existing) return existing;
  const items = loadCustomTopics();
  const topic: Topic = {
    id: Date.now(),
    topicCode: `u-${Date.now()}`,
    topicName: name,
    postCount: 0,
    hot: false
  };
  items.unshift(topic);
  saveCustomTopics(items);
  return topic;
});

mock('get', /\/community\/topics\/[^/]+$/, ({ url }) => {
  const topic = decodeURIComponent(url.split('/').pop() ?? '');
  return aggregateTopics().find((item) => item.topicName === topic || item.topicCode === topic) ?? {
    id: Date.now(),
    topicCode: `missing-${Date.now()}`,
    topicName: topic,
    postCount: 0,
    hot: false
  };
});

mock('get', /\/community\/topics\/[^/]+\/posts$/, ({ url, params }) => {
  const topic = decodeURIComponent(url.split('/').slice(-2)[0]);
  const p = (params ?? {}) as Record<string, unknown>;
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);
  const items = loadPosts()
    .filter((post) => post.topics.includes(topic))
    .sort((a, b) => {
      if (p.sort === 'new') return b.createdAt - a.createdAt;
      return (b.likeCount + b.commentCount) - (a.likeCount + a.commentCount) || b.createdAt - a.createdAt;
    });
  const start = (page - 1) * pageSize;
  return { list: items.slice(start, start + pageSize), page, pageSize, total: items.length };
});

mock('post', /\/community\/follow\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const followed = loadFollow();
  const i = followed.indexOf(id);
  if (i >= 0) {
    followed.splice(i, 1);
    saveFollow(followed);
    return { following: false, followerCount: 0, followeeCount: followed.length };
  }
  followed.push(id);
  saveFollow(followed);
  return { following: true, followerCount: 1, followeeCount: followed.length };
});

mock('get', /\/community\/follow\/me$/, () => loadFollow());

const userFromPost = (id: number, followed: boolean) => {
  const post = loadPosts().find((item) => item.authorId === id);
  return {
    userId: id,
    name: post?.authorName ?? `舞者${String(id).slice(-4)}`,
    avatar: post?.authorAvatar ?? '',
    following: followed,
    followerCount: followed ? 1 : 0,
    followeeCount: id % 5,
    followedAt: Date.now() - id * 1000
  };
};

mock('get', /\/community\/follow\/following$/, () =>
  loadFollow().map((id) => userFromPost(id, true))
);

mock('get', /\/community\/follow\/followers$/, () => {
  const followed = new Set(loadFollow());
  return loadPosts()
    .slice(0, 6)
    .map((post) => userFromPost(post.authorId, followed.has(post.authorId)));
});

mock('get', /\/community\/follow\/\d+\/status$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  return {
    userId: id,
    following: loadFollow().includes(id),
    followerCount: loadFollow().includes(id) ? 1 : 0,
    followeeCount: id % 5
  };
});

mock('get', /\/community\/search$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const q = ((p.q as string) ?? '').toLowerCase();
  const items = loadPosts().filter(
    (it) =>
      it.text.toLowerCase().includes(q) ||
      it.topics.some((t) => t.toLowerCase().includes(q)) ||
      (it.style ?? '').toLowerCase().includes(q) ||
      it.authorName.toLowerCase().includes(q)
  );
  return { list: items, page: 1, pageSize: items.length, total: items.length };
});

mock('get', /\/public\/users\/\d+\/community\/posts$/, ({ url, params }) => {
  const userId = Number(url.split('/').slice(-3)[0]);
  const p = (params ?? {}) as Record<string, unknown>;
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 10);
  let items = loadPosts().filter((it) => it.authorId === userId && it.visibility !== 'private');
  if (items.length === 0 && userId === 1) {
    items = [
      {
        id: 9101,
        authorId: 1,
        authorName: '小李',
        authorAvatar: '',
        text: '今天试听了 Urban Flow 的韩舞课，老师会拆动作，零基础也跟得上。',
        images: [],
        topics: ['Urban Flow', '韩舞'],
        style: '韩舞',
        location: '北京海淀',
        likeCount: 38,
        commentCount: 6,
        collectCount: 3,
        shareCount: 2,
        liked: false,
        collected: false,
        createdAt: Date.now() - 3600_000
      }
    ];
  }
  items = items.slice().sort((a, b) => b.createdAt - a.createdAt);
  const start = (page - 1) * pageSize;
  return { list: items.slice(start, start + pageSize), page, pageSize, total: items.length };
});

mock('get', /\/community\/posts\/me$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 10);
  const items = loadPosts()
    .filter((it) => it.authorId === currentUserId())
    .sort((a, b) => b.createdAt - a.createdAt);
  const start = (page - 1) * pageSize;
  return { list: items.slice(start, start + pageSize), page, pageSize, total: items.length };
});
