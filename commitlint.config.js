module.exports = {
   extends: ['@commitlint/config-conventional'],
  ignores: [
    (message) =>
      /((build\(docker\))|(build\(java\))|(ci)|(docs)): bump .+ from .+ to .+/.test(
        message
      ),
  ],
};
