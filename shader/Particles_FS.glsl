#version 330

#include "shader/ShaderGlobals.glslh"

uniform mat4x4 invCamera;

in vec2 fs_in_tc;
in vec3 fs_in_normal;
in vec4 fs_in_ViewPos;

layout(location = 6) out vec4 fs_out_color;

void main()
{
    vec2 tx = fs_in_tc;
    if(tx.x * tx.x + tx.y * tx.y > 0.5)
    {
       discard;
    }
    fs_out_color = g_color;
}