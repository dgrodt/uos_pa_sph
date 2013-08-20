#version 330

uniform sampler2D g_quadTexture;

in vec2 fs_in_tc;
out vec4 fs_out_color;

void main()
{
	fs_out_color = texture2D(g_quadTexture, fs_in_tc);
}