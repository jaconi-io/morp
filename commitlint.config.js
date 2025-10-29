module.exports = {
   extends: ['@commitlint/config-conventional'],
  ignores: [
    (message) =>
      /((build\(java\))|(ci)|(docs)): bump .+ from .+ to .+/.test(
        message
      ),
  ],
};
