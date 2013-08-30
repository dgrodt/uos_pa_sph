#version 330

#include "shader/ShaderGlobals.glslh"

uniform mat4x4 invCamera;

in vec4 fs_in_ViewPos;
in vec3 fs_in_normal;
in vec2 fs_in_tc;

out vec4 fs_out_color;

void main()
{
    vec2 tx =  2 * fs_in_tc - 1;
    if(tx.x * tx.x + tx.y * tx.y > 1)
    {
       discard;
    }
    fs_out_color = g_color;
}