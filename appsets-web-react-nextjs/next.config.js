/** @type {import('next').NextConfig} */
const nextConfig = {
    images: {
        remotePatterns: [
            {
                protocol: 'https',
                hostname: '**.**.**',
            },
            {
                protocol: 'http',
                hostname: '**.**.**',
            },
            {
                protocol: 'http',
                hostname: '**',
            },
            {
                protocol: 'https',
                hostname: '**',
            },
            ],
    },
    output: 'standalone',
}

module.exports = nextConfig
