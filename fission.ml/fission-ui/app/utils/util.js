/**
 * Created by damien on 24/02/2017.
 */

export function slug(str) {
  // remove accents, swap ñ for n, etc
  const from = 'ãàáäâẽèéëêìíïîõòóöôùúüûñç·/_,:;';
  const to = 'aaaaaeeeeeiiiiooooouuuunc------';

  let strClean = str.replace(/^\s+|\s+$/g, ''); // trim
  strClean = strClean.toLowerCase();

  for (let i = 0, l = from.length; i < l; i += 1) {
    strClean = strClean.replace(new RegExp(from.charAt(i), 'g'), to.charAt(i));
  }

  strClean = strClean.replace(/[^a-z0-9 -]/g, '') // remove invalid chars
    .replace(/\s+/g, '-') // collapse whitespace and replace by -
    .replace(/-+/g, '-'); // collapse dashes

  return strClean;
}

export function decodeBase64(s) {
  return atob(s);
}

export function encodeBase64(s) {
  return btoa(s);
}
