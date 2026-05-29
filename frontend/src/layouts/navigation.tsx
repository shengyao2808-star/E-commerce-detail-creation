// Navigation is now handled directly by SideNav.tsx
// This file is kept for backward compatibility with any remaining imports.
// It will be removed in a future cleanup.

export const navigationItems: never[] = [];
export const getSelectedKey = (_pathname: string): string => "/";
export const defaultOpenKeys: string[] = [];