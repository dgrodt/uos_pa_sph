#version 150

in vec3 vs_in_pos;
in vec2 vs_in_tc;

out vec2 fs_in_tc;
out vec4 gl_Position;

void main()
{
	gl_Position =vec4(vs_in_pos, 1);
	fs_in_tc = vs_in_tc;
}