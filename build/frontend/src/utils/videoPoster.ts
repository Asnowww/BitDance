export const captureVideoPoster = async (url: string, seekToSeconds = 0.1): Promise<string | null> =>
  new Promise((resolve) => {
    const video = document.createElement('video');
    video.muted = true;
    video.playsInline = true;
    video.preload = 'auto';
    video.crossOrigin = 'anonymous';
    video.src = url;

    let settled = false;
    const finish = (value: string | null) => {
      if (settled) return;
      settled = true;
      video.pause();
      video.removeAttribute('src');
      video.load();
      resolve(value);
    };

    const drawFrame = () => {
      try {
        const width = video.videoWidth || 0;
        const height = video.videoHeight || 0;
        if (!width || !height) {
          finish(null);
          return;
        }
        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        const context = canvas.getContext('2d');
        if (!context) {
          finish(null);
          return;
        }
        context.drawImage(video, 0, 0, width, height);
        finish(canvas.toDataURL('image/jpeg', 0.82));
      } catch {
        finish(null);
      }
    };

    video.addEventListener('loadeddata', () => {
      if (!Number.isFinite(video.duration) || video.duration <= 0) {
        drawFrame();
        return;
      }
      const target = Math.min(seekToSeconds, Math.max(video.duration - 0.05, 0));
      if (target <= 0) {
        drawFrame();
        return;
      }
      const onSeeked = () => {
        video.removeEventListener('seeked', onSeeked);
        drawFrame();
      };
      video.addEventListener('seeked', onSeeked, { once: true });
      try {
        video.currentTime = target;
      } catch {
        video.removeEventListener('seeked', onSeeked);
        drawFrame();
      }
    });

    video.addEventListener('error', () => finish(null), { once: true });
  });
