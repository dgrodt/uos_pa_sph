#version 330

#include "shader/ShaderGlobals.glslh"

in vec2 fs_in_tc;
in vec3 fs_in_normal;
in vec4 fs_in_ViewPos;

out vec4 fs_out_color;
//layout(location = 0) out vec4 fs_out_color;
//layout(location = 1) out float fs_out_depth;
void main()
{
    fs_out_color = g_color;
}