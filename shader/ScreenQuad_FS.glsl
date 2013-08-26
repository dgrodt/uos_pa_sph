#version 330

#include "shader/ShaderGlobals.glslh"

uniform sampler2D g_quadTexture;
uniform sampler2D g_particles_normals;
uniform sampler2D g_particles_worldpos;

in vec2 fs_in_tc;
in vec4 fs_in_ViewPos;
out vec4 fs_out_color;

uniform float offset[5] = float[]( 0.0, 1.0, 2.0, 3.0, 4.0 );
uniform float weight[5] = float[]( 0.2270270270, 0.1945945946, 0.1216216216, 0.0540540541, 0.0162162162 );

void main()
{
	float blurSize = setting_blur/768f;
	vec2 fragCoords = vec2(fs_in_tc.x, 1-fs_in_tc.y);
	fs_out_color = texture2D(g_quadTexture, fragCoords);
	/*
	vec3 normal = normalize(cross(texture2D(g_quadTexture, vec2(fragCoords.x - 5, fragCoords.y)).xyz - texture2D(g_quadTexture, fragCoords).xyz, texture2D(g_quadTexture, vec2(fragCoords.x, fragCoords.y - 5)).xyz - texture2D(g_quadTexture, fragCoords).xyz));
    
    vec3 worldView = texture2D(g_quadTexture, fragCoords).xyz;
    
    vec3 lightPos = (g_view * vec4(g_lightPos, 1)).xyz;
    
    vec3 eye = (g_view * vec4(g_eye.xyz,1)).xyz;
    
    vec2 cofs = getSpecDiffuseCoe(normal, eye, worldView, lightPos);*/
	vec4 sum = vec4(0.0);
	   
    // blur in y (vertical)
    // take nine samples, with the distance blurSize between them
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y - 4.0*blurSize)) * 0.05;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y - 3.0*blurSize)) * 0.09;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y - 2.0*blurSize)) * 0.12;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y - blurSize)) * 0.15;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y)) * 0.16;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y + blurSize)) * 0.15;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y + 2.0*blurSize)) * 0.12;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y + 3.0*blurSize)) * 0.09;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y + 4.0*blurSize)) * 0.05;
    
    // blur in x (horizontal)
    // take nine samples, with the distance blurSize between them
    sum += texture2D(g_particles_normals, vec2(fragCoords.x - 4.0*blurSize, fragCoords.y)) * 0.05;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x - 3.0*blurSize, fragCoords.y)) * 0.09;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x - 2.0*blurSize, fragCoords.y)) * 0.12;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x - blurSize, fragCoords.y)) * 0.15;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x, fragCoords.y)) * 0.16;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x + blurSize, fragCoords.y)) * 0.15;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x + 2.0*blurSize, fragCoords.y)) * 0.12;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x + 3.0*blurSize, fragCoords.y)) * 0.09;
    sum += texture2D(g_particles_normals, vec2(fragCoords.x + 4.0*blurSize, fragCoords.y)) * 0.05;
    fs_out_color = fs_out_color + sum;
    fs_out_color.w = 1;
	//fs_out_color = texture2D(g_quadTexture, fs_in_tca);
}