#version 330
#define KERNEL_SIZE 5
#define F (1.0 / float(1 << (2 * (KERNEL_SIZE - 1))))

const float g_pKernel[KERNEL_SIZE * KERNEL_SIZE] = float[](
    1.0,  4.0,  6.0,  4.0, 1.0,
    4.0, 16.0, 24.0, 16.0, 4.0,
    6.0, 24.0, 36.0, 24.0, 6.0,
    4.0, 16.0, 24.0, 16.0, 4.0,
    1.0,  4.0,  6.0,  4.0, 1.0
);
uniform sampler2D g_quadTexture;

in vec2 fs_in_tc;
out vec4 fs_out_color;

void main()
{
    ivec2 sizeOfTexture = textureSize(g_quadTexture, 0);
    vec2 texelSize = vec2(1.0 / float(sizeOfTexture.x), 1.0 / float(sizeOfTexture.y));
    vec4 result = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 own = texture2D(g_quadTexture, fs_in_tc);
    for(int y=0; y < KERNEL_SIZE; ++y)
    {
        for(int x=0; x < KERNEL_SIZE; ++x)
        {
            int offsetX = x - (KERNEL_SIZE / 2);
            int offsetY = y - (KERNEL_SIZE / 2);
            vec2 tex = fs_in_tc + texelSize * vec2(offsetX, offsetY);
            vec4 sampled = texture2D(g_quadTexture, tex);
            result += F * g_pKernel[y * KERNEL_SIZE + x] * sampled;
        }
    }
    fs_out_color = result;
    //fs_out_color = texture2D(g_quadTexture, fs_in_tc);
}