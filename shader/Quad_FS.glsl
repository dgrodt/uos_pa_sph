#version 330

#include "shader/ShaderGlobals.glslh"

in vec2 fs_in_tc;
in vec3 fs_in_normal;
in vec4 fs_in_ViewPos;

//out vec4 fs_out_color;
layout(location = 0) out vec4 fs_out_color;
layout(location = 1) out float fs_out_depth;
void main()
{
	vec3 normal = normalize(fs_in_normal.xyz);
    vec3 worldView = fs_in_ViewPos.xyz;
    vec3 lightPos = (vec4(g_lightPos, 1)).xyz;
    fs_out_color.xyz = g_color.xyz + getLight(worldView, lightPos,  normal);
    fs_out_color.w = g_color.w;
    fs_out_depth = fs_in_ViewPos.z;
}