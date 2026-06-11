export interface DefaultAvatar {
  id: number;
  label: string;
  mark: string;
  background: string;
  foreground: string;
}

export const DEFAULT_AVATARS: DefaultAvatar[] = [
  { id: 101, label: 'BitDance', mark: 'BD', background: '#111111', foreground: '#ffffff' },
  { id: 102, label: 'Hiphop', mark: 'HH', background: '#e8ff5c', foreground: '#111111' },
  { id: 103, label: 'Jazz', mark: 'JZ', background: '#ff7a90', foreground: '#111111' },
  { id: 104, label: 'Breaking', mark: 'BR', background: '#6ee7f9', foreground: '#111111' },
  { id: 105, label: 'Locking', mark: 'LK', background: '#ffd166', foreground: '#111111' },
  { id: 106, label: 'K-pop', mark: 'KP', background: '#a78bfa', foreground: '#ffffff' }
];

export const getDefaultAvatar = (id?: number | string | null) => {
  const normalized = typeof id === 'string' ? Number(id) : id;
  return DEFAULT_AVATARS.find((item) => item.id === normalized);
};
