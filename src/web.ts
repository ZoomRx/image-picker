import { WebPlugin } from '@capacitor/core';
import { ImagePickerPlugin } from './definitions';

export class ImagePickerWeb extends WebPlugin implements ImagePickerPlugin {
  constructor() {
    super({
      name: 'ImagePicker',
      platforms: ['web'],
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

const ImagePicker = new ImagePickerWeb();

export { ImagePicker };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(ImagePicker);
