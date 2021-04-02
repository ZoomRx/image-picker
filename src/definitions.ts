declare module '@capacitor/core' {
  interface PluginRegistry {
    ImagePicker: ImagePickerPlugin;
  }
}

export interface ImagePickerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
